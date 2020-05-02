package kim.hyunsub.videostreaming.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

import kim.hyunsub.videostreaming.models.CustomFile;

@Controller
@RequestMapping(path = "**", params = "!raw")
class ExplorerController {

    @GetMapping
    public String explorer(HttpServletRequest request, Model model) throws Exception {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        ArrayList<CustomFile> files = new ArrayList<>();

        File folder = new File("/archive" + fullPath);

        if (!folder.isDirectory()) {
            throw new ExplorerController.NotDirectoryException("'" + fullPath + "' is not folder");
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            String name = file.getName();
            String size = FileUtils.byteCountToDisplaySize(file.length());
            String path = fullPath + (fullPath.equals("/") ? "" : "/") + name + (file.isFile() ? "?raw" : "");
            CustomFile customFile = new CustomFile(name, path, size);
            files.add(customFile);
        }

        files.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        model.addAttribute("username", "guest");
        model.addAttribute("path", fullPath);
        model.addAttribute("files", files);
        return "explorer";
    }

    static class NotDirectoryException extends Exception {
        private static final long serialVersionUID = 9038584551650821411L;

        public NotDirectoryException(String message) {
            super(message);
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException(ExplorerController.NotDirectoryException e, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.write(e.getMessage());
        writer.close();
    }
}