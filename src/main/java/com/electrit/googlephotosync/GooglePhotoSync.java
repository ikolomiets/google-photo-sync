package com.electrit.googlephotosync;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.List;

public class GooglePhotoSync {

    public static void main(String[] args) throws IOException, InterruptedException {
        Drive googleDriveService = GoogleDriveService.getDriveService();

        String nextPageToken = null;
        do {
            Thread.sleep(500);
            FileList result = googleDriveService.files().list()
                    .setPageSize(100)
                    .setQ("mimeType = 'application/vnd.google-apps.folder' or mimeType = 'image/jpeg'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, mimeType, name, webViewLink, parents)")
                    .setPageToken(nextPageToken)
                    .execute();

            nextPageToken = result.getNextPageToken();
            List<File> files = result.getFiles();
            if (files == null || files.size() == 0) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("id=%s, mimeType=%s, name=%s, link=%s, parent=%s\n",
                            file.getId(), file.getMimeType(), file.getName(), file.getWebViewLink(), file.getParents());
                }
            }
        } while (nextPageToken != null);
    }
}
