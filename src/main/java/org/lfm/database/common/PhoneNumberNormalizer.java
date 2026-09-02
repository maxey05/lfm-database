package org.lfm.database.common;

public final class PhoneNumberNormalizer
{
    private PhoneNumberNormalizer()
    {
    }

    public static String normalize(String raw)
    {
        if(raw == null)
        {
            return null;
        }

        String digits = raw.replaceAll("[^0-9]", "");

        if(digits.isEmpty())
        {
            return null;
        }

        if(digits.length() == 11 && digits.startsWith("09"))
        {
            return "+63" + digits.substring(1);
        }

        if(digits.length() == 12 && digits.startsWith("639"))
        {
            return "+" + digits;
        }

        if(digits.length() == 10 && digits.startsWith("9"))
        {
            return "+63" + digits;
        }

        return raw.trim();
    }
}
