package kim.hyunsub.videostreaming.controllers;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import kim.hyunsub.videostreaming.models.CustomFile;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
class ExplorerController {
    // https://stackoverflow.com/a/3758880
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    @RequestMapping(value = "**")
    public String explorer(HttpServletRequest request, Model model) {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        ArrayList<CustomFile> files = new ArrayList<>();

        File folder = new File("/archive" + fullPath);
        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                File file = listOfFiles[i];
                String name = file.getName();
                String size = ExplorerController.humanReadableByteCountSI(file.length());
                String path = fullPath + (fullPath.equals("/") ? "" : "/") + name;
                CustomFile customFile = new CustomFile(name, path, size);
                files.add(customFile);
            }
        }

        model.addAttribute("username", "guest");
        model.addAttribute("path", fullPath);
        model.addAttribute("files", files);
        return "explorer";
    }
}