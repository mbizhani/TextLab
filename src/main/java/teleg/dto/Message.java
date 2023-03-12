package teleg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Message {
	private Long id;

	private Date date;

	private String from;

	@JsonProperty("text_entities")
	private List<TextEntity> textEntities;

	// ------------------------------

	@Getter
	@Setter
	public static class TextEntity {
		private String type;
		private String text;
	}
}
