package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final WhatsAppMetaService whatsAppService;

    /**
     * Send interactive menu to member (formatted as text with options)
     */
    public void sendMainMenu(String phoneNumber, String memberName) {
        String menu = buildMainMenu(memberName);
        whatsAppService.sendMessage(phoneNumber, menu);
        log.info("📱 Main menu sent to: {}", phoneNumber);
    }

    /**
     * Send offerings menu
     */
    public void sendOfferingsMenu(String phoneNumber) {
        String menu = buildOfferingsMenu();
        whatsAppService.sendMessage(phoneNumber, menu);
        log.info("💰 Offerings menu sent to: {}", phoneNumber);
    }

    /**
     * Send info menu
     */
    public void sendInfoMenu(String phoneNumber) {
        String menu = buildInfoMenu();
        whatsAppService.sendMessage(phoneNumber, menu);
        log.info("ℹ️ Info menu sent to: {}", phoneNumber);
    }

    /**
     * Build main menu
     */
    private String buildMainMenu(String memberName) {
        return "🏠 *ChurchConnect Main Menu*\n\n" +
                "Hello *" + memberName + "*! 👋\n\n" +
                "What would you like to do today?\n\n" +
                "─────────────────\n\n" +
                "*Quick Actions:*\n\n" +
                "1️⃣ 💰 *GIVE* - Make offering\n" +
                "2️⃣ 📊 *BALANCE* - Check giving\n" +
                "3️⃣ 🙏 *PRAYER* - Prayer request\n" +
                "4️⃣ ℹ️ *INFO* - My details\n" +
                "5️⃣ ❓ *HELP* - All commands\n\n" +
                "─────────────────\n\n" +
                "_Just type the number or command!_ 😊\n\n" +
                "Example: Type *1* or *GIVE 1000*";
    }

    /**
     * Build offerings menu
     */
    private String buildOfferingsMenu() {
        return "💰 *Offerings Menu*\n\n" +
                "Choose an option:\n\n" +
                "1️⃣ *Quick Give*\n" +
                "   Type: GIVE 100\n" +
                "   _Instant M-PESA prompt_\n\n" +
                "2️⃣ *Check Balance*\n" +
                "   Type: BALANCE\n" +
                "   _View your giving history_\n\n" +
                "3️⃣ *Manual M-PESA*\n" +
                "   Type: INFO\n" +
                "   _Get PayBill details_\n\n" +
                "─────────────────\n\n" +
                "💡 *Quick Amounts:*\n" +
                "• Type *GIVE 100*\n" +
                "• Type *GIVE 500*\n" +
                "• Type *GIVE 1000*\n\n" +
                "Type *MENU* to go back";
    }

    /**
     * Build info menu
     */
    private String buildInfoMenu() {
        return "ℹ️ *Information Menu*\n\n" +
                "What would you like to know?\n\n" +
                "1️⃣ *My Details*\n" +
                "   Type: INFO\n" +
                "   _View membership info_\n\n" +
                "2️⃣ *Church Info*\n" +
                "   Type: LOCATION\n" +
                "   _Address & contact_\n\n" +
                "3️⃣ *Service Times*\n" +
                "   Type: TIMES\n" +
                "   _Worship schedule_\n\n" +
                "4️⃣ *Help & Commands*\n" +
                "   Type: HELP\n" +
                "   _See all commands_\n\n" +
                "─────────────────\n\n" +
                "Type *MENU* to go back";
    }

    /**
     * Handle menu selection
     */
    public String handleMenuSelection(String input, String memberName) {
        String selection = input.trim().toLowerCase();

        switch (selection) {
            case "1":
            case "menu":
                return buildMainMenu(memberName);

            case "2":
            case "offerings":
                return buildOfferingsMenu();

            case "3":
            case "info menu":
                return buildInfoMenu();

            default:
                return "❓ Invalid selection. Type *MENU* to see options.";
        }
    }

    /**
     * Send custom menu (for future WhatsApp native buttons)
     */
    public void sendCustomMenu(String phoneNumber, MenuRequest menuRequest) {
        // Future: Implement WhatsApp native interactive buttons
        // For now, send formatted text
        StringBuilder menu = new StringBuilder();
        menu.append("📋 *").append(menuRequest.getTitle()).append("*\n\n");
        menu.append(menuRequest.getDescription()).append("\n\n");
        menu.append("─────────────────\n\n");

        for (int i = 0; i < menuRequest.getOptions().size(); i++) {
            menu.append((i + 1)).append("️⃣ ")
                    .append(menuRequest.getOptions().get(i))
                    .append("\n");
        }

        menu.append("\n_Type the number of your choice_");

        whatsAppService.sendMessage(phoneNumber, menu.toString());
        log.info("📱 Custom menu sent to: {}", phoneNumber);
    }
}