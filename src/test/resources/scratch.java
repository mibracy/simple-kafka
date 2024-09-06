private static final ZoneId CENTRAL_TIME_ZONE = ZoneId.of("America/Chicago");
private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

public static String formatDateTimeInCentralTimeZone(String dateTimeStr) {
    try {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        if (zonedDateTime.getZone().equals(CENTRAL_TIME_ZONE)) {
            return dateTimeStr; // Already in Central Time Zone
        } else {
            return zonedDateTime.withZoneSameInstant(CENTRAL_TIME_ZONE).format(DATE_TIME_FORMATTER); // Convert to Central Time Zone
        }
    } catch (Exception e) {
        // Handle invalid date-time string
        return "Invalid date-time string: " + dateTimeStr;
    }
}