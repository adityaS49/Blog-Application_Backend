package com.blogWebsite.BlogWebsite.util;

import java.text.Normalizer;

public class SlugUtil {

    // Convert a string into a URL-friendly slug
    public static String toSlug(String input) {
        if (input == null) return null;

        // 1. Convert to lowercase
        String slug = input.toLowerCase();

        // 2. Remove accents/diacritics (é -> e)
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 3. Replace non-alphanumeric characters with hyphens
        slug = slug.replaceAll("[^a-z0-9]+", "-");

        // 4. Remove leading/trailing hyphens
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}
