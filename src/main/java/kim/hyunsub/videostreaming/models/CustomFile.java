package kim.hyunsub.videostreaming.models;

import lombok.Data;

@Data
public class CustomFile {
    private final String name;
    private final String path;
    private final String size;
}