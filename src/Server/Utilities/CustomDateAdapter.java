// Group: 6
package Server.Utilities;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDateAdapter extends TypeAdapter<Date> {

    private final SimpleDateFormat dateFormat;

    public CustomDateAdapter() {
        // Define the date format with Locale.ENGLISH
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm:ss a", Locale.ENGLISH);
    }

    public static Gson gsonCreator() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new CustomDateAdapter())  // Use the custom adapter
                .create();
    }

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        out.value(dateFormat.format(value));  // Write the Date as a formatted string
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        String dateStr = in.nextString();  // Read the date string

        // Replace the narrow no-break space (U+202F) with a regular space (U+0020)
        dateStr = dateStr.replace("\u202F", " ");

        try {
            return dateFormat.parse(dateStr);  // Parse the string into a Date object
        } catch (ParseException e) {
            throw new JsonSyntaxException("Failed to parse date: " + dateStr, e);
        }
    }
}
