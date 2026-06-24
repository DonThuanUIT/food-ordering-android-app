package com.foodorderingapp.utils;

public class CategoryIconHelper {
    public static String getCategoryEmoji(String categoryName) {
        if (categoryName == null) return "";
        String lower = categoryName.toLowerCase(java.util.Locale.ROOT).trim();
        if (lower.contains("mì cay")) return "🌶️🍜";
        if (lower.contains("mì") || lower.contains("noodle") || lower.contains("ramen") || lower.contains("hủ tiếu") || lower.contains("phở")) return "🍜";
        if (lower.contains("kimbap") || lower.contains("sushi") || lower.contains("cơm cuộn")) return "🍣";
        if (lower.contains("cơm") || lower.contains("rice")) return "🍚";
        if (lower.contains("gà") || lower.contains("chicken")) return "🍗";
        if (lower.contains("burger") || lower.contains("hamburger")) return "🍔";
        if (lower.contains("pizza")) return "🍕";
        if (lower.contains("ăn vặt") || lower.contains("snack") || lower.contains("chiên") || lower.contains("khoai tây")) return "🍟";
        if (lower.contains("trà sữa") || lower.contains("boba") || lower.contains("bubble")) return "🧋";
        if (lower.contains("trà") || lower.contains("tea")) return "🍵";
        if (lower.contains("uống") || lower.contains("nước") || lower.contains("drink") || lower.contains("coke") || lower.contains("coca") || lower.contains("pepsi") || lower.contains("soda") || lower.contains("juice") || lower.contains("sinh tố")) return "🥤";
        if (lower.contains("cà phê") || lower.contains("coffee") || lower.contains("cafe")) return "☕";
        if (lower.contains("bánh") || lower.contains("cake") || lower.contains("bread")) return "🍰";
        if (lower.contains("kem") || lower.contains("ice cream")) return "🍦";
        if (lower.contains("lẩu") || lower.contains("hotpot")) return "🍲";
        if (lower.contains("súp") || lower.contains("soup") || lower.contains("canh")) return "🥣";
        if (lower.contains("tất cả") || lower.contains("all")) return "✨";
        return "🍽️"; // Default
    }

    public static String getEmojiPrefix(String categoryName) {
        String emoji = getCategoryEmoji(categoryName);
        return emoji.isEmpty() ? "" : emoji + " ";
    }

    public static String getEmojiForDisplay(String fullName) {
        if (fullName == null) return "🍽️";
        if (fullName.contains("|")) {
            String[] parts = fullName.split("\\|");
            return parts[0].trim();
        }
        return getCategoryEmoji(fullName);
    }

    public static String getNameForDisplay(String fullName) {
        if (fullName == null) return "";
        if (fullName.contains("|")) {
            String[] parts = fullName.split("\\|");
            return parts.length > 1 ? parts[1].trim() : parts[0].trim();
        }
        return fullName;
    }
}
