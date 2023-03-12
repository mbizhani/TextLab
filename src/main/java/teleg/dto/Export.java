package teleg.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Export {
	private String name;
	private List<Message> messages;
}
