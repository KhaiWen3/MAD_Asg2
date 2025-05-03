package my.edu.madgroupassignment;

import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;

public class CategoryColors {
    private static final Map<String, Integer> colorMap = new HashMap<>();

    static {
        // Initialize colors for different categories
        colorMap.put("Education", Color.parseColor("#F44336")); // Red
        colorMap.put("Shopping", Color.parseColor("#4CAF50")); // Green
        colorMap.put("Meal", Color.parseColor("#FF9800")); // Orange
        colorMap.put("Health", Color.parseColor("#2196F3")); // Blue
        colorMap.put("Other", Color.parseColor("#9C27B0")); // Purple
        // Default color for custom categories
        colorMap.put("DEFAULT", Color.parseColor("#607D8B")); // Blue Grey
    }

    public static int getColorForCategory(String category) {
        return colorMap.getOrDefault(category, colorMap.get("DEFAULT"));
    }
}