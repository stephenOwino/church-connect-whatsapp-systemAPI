package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MemberRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final MemberRepository memberRepository;
    private final OfferingRepository offeringRepository;
    private final ChurchRepository churchRepository;
    private final AnnouncementRepository announcementRepository;
    private final WhatsAppMetaService whatsAppMetaService;
    private final MpesaService mpesaService;
    private final MessageService messageService;
    private final CommandService commandService;
    private final PastorQueueService pastorQueueService;
    private final ConversationService conversationService;

    /**
     * Process incoming WhatsApp message and generate response
     */
    @Transactional
    public void processIncomingMessage(String from, String messageBody, String messageSid) {
        long startTime = System.currentTimeMillis();
        log.info("📩 Received message from {}: {}", from, messageBody);

        String phoneNumber = from.replace("whatsapp:", "").trim();

        // Get church (assuming first church for now - enhance later for multi-church)
        Church church = churchRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No church found"));

        // Save incoming message
        Message incomingMessage = messageService.saveMessage(
                church.getId(), phoneNumber, "INBOUND", messageBody, messageSid);

        // Check for delivery receipts
        if (messageBody.toLowerCase().contains("delivered") ||
                messageBody.toLowerCase().contains("read")) {
            handleDeliveryReceipt(phoneNumber, messageBody);
            return;
        }

        Optional<Member> memberOpt = memberRepository.findByPhoneNumber(phoneNumber);

        String response;
        String commandType = null;
        boolean isCommand = false;
        boolean needsPastorReply = false;

        if (memberOpt.isEmpty()) {
            if (messageBody.toLowerCase().startsWith("register")) {
                response = handleRegistration(phoneNumber, messageBody, church);
                commandType = "REGISTER";
                isCommand = true;
            } else {
                response = sendWelcomeMessage(phoneNumber);
            }
        } else {
            Member member = memberOpt.get();

            // Detect command type
            commandType = detectCommandType(messageBody);
            isCommand = commandType != null;

            // Check if needs pastor reply (long messages, prayer requests, etc.)
            needsPastorReply = shouldEscalateToPastor(messageBody, commandType);

            response = generateResponse(member, messageBody.trim());
        }

        // Update message flags
        if (isCommand) {
            messageService.markAsCommand(incomingMessage.getId(), commandType);
        }
        if (needsPastorReply) {
            messageService.markNeedsPastorReply(incomingMessage.getId(), true);
            // Add to pastor queue
            addToPastorQueue(church.getId(), incomingMessage, messageBody);
        }

        // Send response
        String sentMessageSid = whatsAppMetaService.sendMessage(phoneNumber, response);

        // Save outbound message
        messageService.saveMessage(
                church.getId(), phoneNumber, "OUTBOUND", response, sentMessageSid);

        // Log command execution
        long executionTime = System.currentTimeMillis() - startTime;
        if (isCommand && memberOpt.isPresent()) {
            commandService.logCommand(
                    church.getId(),
                    phoneNumber,
                    commandType,
                    messageBody,
                    null, // parameters - can be enhanced
                    true,
                    null,
                    response,
                    executionTime
            );
        }

        log.info("✅ Message processed in {}ms", executionTime);
    }

    /**
     * Detect command type from message
     */
    private String detectCommandType(String message) {
        String lower = message.toLowerCase().trim();

        if (lower.startsWith("register")) return "REGISTER";
        if (lower.startsWith("give") || lower.matches("give\\s+\\d+")) return "GIVE";
        if (lower.matches(".*(balance|offerings|my offerings|check balance|total).*")) return "BALANCE";
        if (lower.matches(".*(prayer|pray|intercession|request prayer|ombi).*")) return "PRAYER";
        if (lower.matches(".*(info|information|details|my details).*")) return "INFO";
        if (lower.matches(".*(help|menu|commands|msaada).*")) return "HELP";

        return null;
    }

    /**
     * Check if message should be escalated to pastor
     */
    private boolean shouldEscalateToPastor(String message, String commandType) {
        // Prayer requests always go to pastor
        if ("PRAYER".equals(commandType)) return true;

        // Long messages (likely questions/concerns)
        if (message.length() > 200) return true;

        // Messages with keywords requiring pastor attention
        String lower = message.toLowerCase();
        if (lower.contains("counseling") || lower.contains("counsel") ||
                lower.contains("problem") || lower.contains("issue") ||
                lower.contains("help me") || lower.contains("confused") ||
                lower.contains("difficult") || lower.contains("struggling")) {
            return true;
        }

        return false;
    }

    /**
     * Add message to pastor queue
     */
    private void addToPastorQueue(Long churchId, Message message, String messageBody) {
        try {
            String category = "OTHER";
            String priority = "MEDIUM";

            String lower = messageBody.toLowerCase();
            if (lower.contains("prayer") || lower.contains("pray")) {
                category = "PRAYER";
            } else if (lower.contains("counsel") || lower.contains("advice")) {
                category = "COUNSELING";
            } else if (lower.contains("urgent") || lower.contains("emergency")) {
                category = "INQUIRY";
                priority = "HIGH";
            } else if (lower.contains("complaint") || lower.contains("issue")) {
                category = "COMPLAINT";
                priority = "HIGH";
            }

            com.stephenotieno.church_whatsapp_system.churchconnect.dto.PastorQueueRequest request =
                    com.stephenotieno.church_whatsapp_system.churchconnect.dto.PastorQueueRequest.builder()
                            .messageId(message.getId())
                            .category(category)
                            .priority(priority)
                            .build();

            pastorQueueService.addToQueue(churchId, request);
            log.info("📬 Message added to pastor queue");

        } catch (Exception e) {
            log.error("❌ Failed to add to pastor queue: {}", e.getMessage());
        }
    }

    /**
     * Generate intelligent response based on message content
     */
    private String generateResponse(Member member, String message) {
        String lowerMessage = message.toLowerCase();

        // === GREETINGS ===
        if (lowerMessage.matches(".*(hello|hi|hey|good morning|good afternoon|good evening|habari|mambo).*")) {
            return handleGreeting(member);
        }

        // === SELF REGISTRATION ===
        if (lowerMessage.startsWith("register")) {
            return "✅ *Registration Status*\n\n" +
                    "You're already registered as:\n" +
                    "*" + member.getFullName() + "*\n\n" +
                    "Type *INFO* to view your full details.";
        }

        // === CHECK BALANCE ===
        if (lowerMessage.matches(".*(balance|offerings|my offerings|check balance|total).*")) {
            return getOfferingBalance(member);
        }

        // === GIVE/OFFERING WITH AMOUNT ===
        Pattern givePattern = Pattern.compile("give\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher giveMatcher = givePattern.matcher(message);

        if (giveMatcher.find()) {
            String amount = giveMatcher.group(1);
            return initiatePayment(member, amount);
        }

        // === GIVE (general) ===
        if (lowerMessage.matches(".*(give|offering|donate|tithe|sadaka|mchango).*")) {
            return getPaymentInstructions(member);
        }

        // === PRAYER REQUEST ===
        if (lowerMessage.matches(".*(prayer|pray|intercession|request prayer|ombi).*")) {
            return handlePrayerRequest(member, message);
        }

        // === CHURCH INFO ===
        if (lowerMessage.matches(".*(info|information|details|my details|location|address).*")) {
            return getMemberInfo(member);
        }

        // === MEMBERSHIP INFO ===
        if (lowerMessage.matches(".*(membership|member|status).*")) {
            return getMembershipDetails(member);
        }

        // === HELP MENU ===
        if (lowerMessage.matches(".*(help|menu|commands|msaada).*")) {
            return getHelpMenu();
        }

        // === CONFIRM MESSAGE DELIVERY ===
        if (lowerMessage.matches(".*(confirm|received|got it|nimepokea).*")) {
            return "✅ *Confirmed!*\n\n" +
                    "Thank you *" + member.getFullName() + "*\n" +
                    "Your confirmation has been recorded 📝\n\n" +
                    "_God bless you!_ 🙏";
        }

        // === THANK YOU ===
        if (lowerMessage.matches(".*(thank|thanks|asante|appreciate).*")) {
            return "You're welcome *" + member.getFullName() + "*! 😊\n\n" +
                    "_Happy to serve you_\n" +
                    "God bless! 🙏";
        }

        // === AMEN ===
        if (lowerMessage.matches(".*(amen|hallelujah|praise|glory|amina).*")) {
            return "🙌 *Amen and Amen!*\n\n" +
                    "_All glory to God!_ 🙏✨";
        }

        // === YES RESPONSES ===
        if (lowerMessage.matches("^(yes|yeah|yep|ok|okay|ndio|sawa)$")) {
            return "Great! How can I assist you?\n\n" +
                    "Type *HELP* to see available commands 😊";
        }

        // === CANCEL/STOP ===
        if (lowerMessage.matches(".*(cancel|stop|unsubscribe|acha).*")) {
            return handleUnsubscribe(member);
        }

        // === PASTOR REPLY (checking if message is a reply) ===
        if (message.length() > 50) {
            return "📨 *Message Received*\n\n" +
                    "Thank you *" + member.getFullName() + "*\n\n" +
                    "Your message has been forwarded to church leadership. Someone will get back to you soon.\n\n" +
                    "_God bless!_ 🙏";
        }

        // === DEFAULT RESPONSE ===
        return "🤔 I didn't quite understand that *" + member.getFullName() + "*\n\n" +
                "*Try these commands:*\n" +
                "• HELP - Show all commands\n" +
                "• BALANCE - Check offerings\n" +
                "• GIVE [amount] - Make offering\n" +
                "• PRAYER - Request prayer\n" +
                "• INFO - Membership details\n\n" +
                "_Or just type your question!_ 😊";
    }

    /**
     * Handle new member registration
     */
    @Transactional
    private String handleRegistration(String phoneNumber, String message, Church church) {
        Pattern pattern = Pattern.compile("register\\s+(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);

        if (!matcher.find()) {
            return "❌ *Invalid Format*\n\n" +
                    "Please use:\n" +
                    "*REGISTER [Your Full Name]*\n\n" +
                    "_Example:_ REGISTER John Doe";
        }

        String fullName = matcher.group(1).trim();

        if (fullName.split("\\s+").length < 2) {
            return "❌ *Name Required*\n\n" +
                    "Please provide your full name\n" +
                    "(first and last name)\n\n" +
                    "_Example:_ REGISTER John Doe";
        }

        if (memberRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            return "⚠️ *Already Registered*\n\n" +
                    "This number is already registered!\n\n" +
                    "Type *INFO* to view your details.";
        }

        Member newMember = Member.builder()
                .church(church)
                .phoneNumber(phoneNumber)
                .fullName(fullName)
                .status("ACTIVE")
                .build();

        memberRepository.save(newMember);
        log.info("✅ New member registered: {} ({})", fullName, phoneNumber);

        return "🎉 *Welcome to " + church.getName() + "!*\n\n" +
                "✅ Registration complete!\n\n" +
                "*Your Details:*\n" +
                "📝 Name: " + fullName + "\n" +
                "📱 Phone: " + phoneNumber + "\n" +
                "⛪ Church: " + church.getName() + "\n\n" +
                "─────────────────\n\n" +
                "Type *HELP* to see what you can do\n\n" +
                "_God bless you!_ 🙏";
    }

    /**
     * Handle greeting
     */
    private String handleGreeting(Member member) {
        String timeOfDay = getTimeOfDay();

        return "🙏 Good " + timeOfDay + " *" + member.getFullName() + "*!\n\n" +
                "Welcome to *" + member.getChurch().getName() + "*\n\n" +
                "*Quick Actions:*\n" +
                "💰 GIVE [amount] - Make offering\n" +
                "📊 BALANCE - Check offerings\n" +
                "🙏 PRAYER - Request prayer\n" +
                "📍 INFO - Your details\n" +
                "❓ HELP - All commands\n\n" +
                "_How can I help you today?_ 😊";
    }

    /**
     * Initiate M-PESA payment
     */
    private String initiatePayment(Member member, String amount) {
        try {
            Double amountValue = Double.parseDouble(amount);

            if (amountValue < 1) {
                return "❌ *Invalid Amount*\n\n" +
                        "Minimum amount is KES 1";
            }

            mpesaService.initiateSTKPush(
                    member.getPhoneNumber(),
                    amountValue,
                    "Offering - " + member.getFullName()
            );

            return "📱 *M-PESA Payment Initiated*\n\n" +
                    "💰 Amount: *KES " + amount + "*\n\n" +
                    "Check your phone for M-PESA prompt\n\n" +
                    "*Steps:*\n" +
                    "1️⃣ Enter your M-PESA PIN\n" +
                    "2️⃣ Confirm the payment\n" +
                    "3️⃣ Receive confirmation SMS\n\n" +
                    "─────────────────\n\n" +
                    "_Thank you for your generous giving!_ 🙏\n\n" +
                    "~God loves a cheerful giver~\n" +
                    "_- 2 Corinthians 9:7_";

        } catch (NumberFormatException e) {
            return "❌ *Invalid Amount*\n\n" +
                    "Please use numbers only\n\n" +
                    "_Example:_ *GIVE 1000*";
        }
    }

    /**
     * Get payment instructions
     */
    private String getPaymentInstructions(Member member) {
        Church church = member.getChurch();

        return "💰 *Make an Offering*\n\n" +
                "*Option 1: Quick Payment* ⚡\n" +
                "Type: *GIVE [amount]*\n" +
                "_Example:_ GIVE 1000\n" +
                "Get instant M-PESA prompt!\n\n" +
                "─────────────────\n\n" +
                "*Option 2: Manual M-PESA*\n" +
                "1️⃣ Go to M-PESA menu\n" +
                "2️⃣ Select _Lipa na M-PESA_\n" +
                "3️⃣ Choose _Pay Bill_\n" +
                "4️⃣ Business No: *888880*\n" +
                "5️⃣ Account: *" + member.getFullName() + "*\n" +
                "6️⃣ Enter amount\n" +
                "7️⃣ Enter your PIN\n\n" +
                "─────────────────\n\n" +
                "🙏 _Thank you for supporting_ " + church.getName() + "\n\n" +
                "~Bring the whole tithe into the storehouse~\n" +
                "_- Malachi 3:10_";
    }

    /**
     * Get offering balance
     */
    private String getOfferingBalance(Member member) {
        List<Offering> offerings = offeringRepository.findAll().stream()
                .filter(o -> o.getMember() != null &&
                        o.getMember().getId().equals(member.getId()) &&
                        "COMPLETED".equals(o.getStatus()))
                .toList();

        BigDecimal totalOfferings = offerings.stream()
                .map(Offering::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Offering> recentOfferings = offerings.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(3)
                .toList();

        StringBuilder response = new StringBuilder();
        response.append("💰 *Your Offering Summary*\n\n");
        response.append("👤 *").append(member.getFullName()).append("*\n\n");
        response.append("💵 Total Given: *KES ").append(totalOfferings).append("*\n");
        response.append("📊 Offerings: *").append(offerings.size()).append("*\n\n");
        response.append("─────────────────\n\n");

        if (!recentOfferings.isEmpty()) {
            response.append("*Recent Offerings:*\n");
            for (Offering offering : recentOfferings) {
                response.append("• KES ").append(offering.getAmount())
                        .append(" _- ")
                        .append(offering.getCreatedAt().format(
                                DateTimeFormatter.ofPattern("dd MMM yyyy")))
                        .append("_\n");
            }
            response.append("\n");
        }

        response.append("─────────────────\n\n");
        response.append("🙏 _Thank you for your faithful giving!_\n\n");
        response.append("~God loves a cheerful giver~\n");
        response.append("_- 2 Corinthians 9:7_");

        return response.toString();
    }

    /**
     * Handle prayer request
     */
    private String handlePrayerRequest(Member member, String message) {
        log.info("🙏 Prayer request from {}: {}", member.getFullName(), message);

        return "🙏 *Prayer Request Received*\n\n" +
                "Dear *" + member.getFullName() + "*\n\n" +
                "Your prayer request has been received and forwarded to our prayer team.\n\n" +
                "_We are standing with you in faith!_\n\n" +
                "─────────────────\n\n" +
                "~The prayer of a righteous person is powerful and effective~\n" +
                "_- James 5:16_\n\n" +
                "Stay blessed! 🕊️";
    }

    /**
     * Get member info
     */
    private String getMemberInfo(Member member) {
        Church church = member.getChurch();

        long daysAsMember = java.time.temporal.ChronoUnit.DAYS.between(
                member.getJoinedDate() != null ? member.getJoinedDate().atStartOfDay() : LocalDateTime.now(),
                LocalDateTime.now()
        );

        return "👤 *Your Membership Details*\n\n" +
                "*Personal Information:*\n" +
                "📝 Name: " + member.getFullName() + "\n" +
                "📱 Phone: " + member.getPhoneNumber() + "\n" +
                "✅ Status: " + member.getStatus() + "\n" +
                "📅 Member Since: " + (member.getJoinedDate() != null ?
                member.getJoinedDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) :
                "N/A") + "\n" +
                "⏱️ Days: " + daysAsMember + " days\n\n" +
                "─────────────────\n\n" +
                "*Church Information:*\n" +
                "⛪ " + church.getName() + "\n" +
                "📍 " + church.getLocation() + "\n" +
                "📞 " + church.getPhone() + "\n\n" +
                "─────────────────\n\n" +
                "*Service Times:*\n" +
                "🌅 Sunday: _9:00 AM - 12:00 PM_\n" +
                "📖 Wednesday: _6:00 PM (Bible Study)_\n" +
                "🙏 Friday: _6:00 PM (Prayer Meeting)_\n\n" +
                "_We look forward to seeing you!_ 🙏";
    }

    /**
     * Get membership details
     */
    private String getMembershipDetails(Member member) {
        return getMemberInfo(member);
    }

    /**
     * Get help menu
     */
    private String getHelpMenu() {
        return "📋 *ChurchConnect Commands*\n\n" +
                "*🔹 Registration*\n" +
                "• REGISTER [Name] - Join church\n" +
                "_Example: REGISTER John Doe_\n\n" +
                "*🔹 Offerings*\n" +
                "• GIVE [amount] - Quick M-PESA\n" +
                "_Example: GIVE 1000_\n" +
                "• BALANCE - Check offerings\n\n" +
                "*🔹 Information*\n" +
                "• INFO - Membership details\n" +
                "• PRAYER - Submit prayer request\n\n" +
                "*🔹 Other Commands*\n" +
                "• HELP - Show this menu\n" +
                "• CONFIRM - Confirm receipt\n\n" +
                "─────────────────\n\n" +
                "*💬 Swahili Commands*\n" +
                "• HABARI - Salamu\n" +
                "• MSAADA - Help\n" +
                "• SADAKA [kiasi] - Toa sadaka\n" +
                "• OMBI - Ombi la maombi\n\n" +
                "─────────────────\n\n" +
                "_Just type your question!_ 😊";
    }

    /**
     * Handle unsubscribe
     */
    private String handleUnsubscribe(Member member) {
        member.setStatus("INACTIVE");
        memberRepository.save(member);

        return "😢 *Unsubscribed*\n\n" +
                "We're sorry to see you go *" + member.getFullName() + "*\n\n" +
                "You've been unsubscribed from automated messages.\n\n" +
                "To reactivate, type *REGISTER* anytime.\n\n" +
                "_God bless you!_ 🙏";
    }

    /**
     * Send welcome message
     */
    private String sendWelcomeMessage(String phoneNumber) {
        return "👋 *Welcome to ChurchConnect!*\n\n" +
                "It looks like you're new here! 🎉\n\n" +
                "─────────────────\n\n" +
                "*To Get Started:*\n\n" +
                "Register by typing:\n" +
                "*REGISTER [Your Full Name]*\n\n" +
                "_Example:_\n" +
                "REGISTER John Doe\n\n" +
                "─────────────────\n\n" +
                "*After registration:*\n" +
                "✅ Give offerings via M-PESA\n" +
                "✅ Check giving history\n" +
                "✅ Request prayer\n" +
                "✅ Get church updates\n\n" +
                "_We look forward to having you!_ 🙏";
    }

    /**
     * Handle delivery receipt
     */
    private void handleDeliveryReceipt(String phoneNumber, String message) {
        log.info("✅ Delivery receipt from {}: {}", phoneNumber, message);
    }

    /**
     * Get time of day
     */
    private String getTimeOfDay() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "morning";
        if (hour < 17) return "afternoon";
        return "evening";
    }
}