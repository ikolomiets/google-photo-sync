package com.electrit.googlephotosync;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.*;

public class GooglePhotoSync {

    public static final String MIME_APPS_FOLDER = "application/vnd.google-apps.folder";
    public static final String MIME_IMAGE = "image/jpeg";

    private final Set<Entry> topEntries = new HashSet<Entry>();

    public Set<Entry> buildTopEntries() throws IOException {
        Drive googleDriveService = GoogleDriveService.getDriveService();

        String nextPageToken = null;
        do {
            FileList result = googleDriveService.files().list()
                    .setPageSize(100)
                    .setQ("mimeType = '" + MIME_APPS_FOLDER + "' or mimeType = '" + MIME_IMAGE + "'")
                    .setFields("nextPageToken, files(id, mimeType, name, webViewLink, parents)")
                    .setPageToken(nextPageToken)
                    .execute();

            nextPageToken = result.getNextPageToken();
            List<File> files = result.getFiles();
            if (files == null || files.size() == 0) {
                System.out.println("No files found.");
            } else {
                //System.out.println("Files:");
                for (File file : files) {

                    if (file.getParents() != null && file.getParents().get(0).equals("0B37lk-sxC9NMNXhKcll0VGM2eFU")) {
                        Entry e = new Entry(file.getId(), file.getParents().get(0), file.getName(), file.getWebViewLink());
                        System.out.println("XXX " + e.toString());
                    }
/*
                    System.out.printf("id=%s, mimeType=%s, name=%s, link=%s, parent=%s\n",
                            file.getId(), file.getMimeType(), file.getName(), file.getWebViewLink(), file.getParents());
*/

                    if (file.getParents() == null) {
                        System.out.println("skipping (parents=null)");
                        continue;
                    }

                    Entry entry;
                    if (MIME_APPS_FOLDER.equals(file.getMimeType())) {
                        entry = new Folder(file.getId(), file.getParents().get(0), file.getName(), file.getWebViewLink());
                    } else if (MIME_IMAGE.equals(file.getMimeType())) {
                        entry = new Entry(file.getId(), file.getParents().get(0), file.getName(), file.getWebViewLink());
                    } else {
                        throw new IllegalArgumentException("Invalid mimeType: " + file.getMimeType());
                    }

                    addEntry(entry);
                }
            }
        } while (nextPageToken != null);

        return Collections.unmodifiableSet(topEntries);
    }

    private Folder addEntry(Entry entry) {

        Folder parentFolder = null;
        for (Iterator<Entry> iterator = topEntries.iterator(); iterator.hasNext(); ) {
            Entry topEntry = iterator.next();
            if (parentFolder == null && topEntry instanceof Folder) {
                parentFolder = addEntry((Folder) topEntry, entry);;
            }

            if (entry.getId().equals(topEntry.getParendId())) {
                ((Folder) entry).addChild(topEntry);
                topEntry.setParentFolder((Folder) entry);
                iterator.remove();
            }
        }

        if (parentFolder == null)
            topEntries.add(entry);

        return null;
    }

    private Folder addEntry(Folder parent, Entry entry) {
        if (parent.getId().equals(entry.getParendId())) {
            parent.addChild(entry);
            entry.setParentFolder(parent);
            return parent;
        } else {
            for (Entry child : parent.getChildren()) {
                if (child instanceof Folder) {
                    Folder result = addEntry((Folder) child, entry);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GooglePhotoSync googlePhotoSync = new GooglePhotoSync();
        Set<Entry> topEntries = googlePhotoSync.buildTopEntries();
        for (Entry topEntry : topEntries) {
            if (topEntry.getName().equals("classified-ads")) {
                System.out.println(topEntry.toString());
                break;
            }
        }
    }
}
