package teleg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import teleg.dto.Export;
import teleg.dto.Message;

import java.io.File;
import java.io.IOException;

public class ChannelReader {
	public static void loadMessages(File file) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			//mapper.registerModule(new JavaTimeModule());

			final Export export = mapper.readValue(file, Export.class);
			for (Message message : export.getMessages()) {
				System.out.println("message = " + message);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		loadMessages(new File("chats/bbc_sample.json"));
	}
}
