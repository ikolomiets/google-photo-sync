package com.electrit.googlephotosync;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GooglePhotoSync {

    private final static Logger logger = LoggerFactory.getLogger(GooglePhotoSync.class);

    private static final String BASE_DIR = "../classified-ads/src";
    private static final String MIME_APPS_FOLDER = "application/vnd.google-apps.folder";
    private static final String MIME_IMAGE = "image/jpeg";

    private final Set<Entry> topEntries = new HashSet<Entry>();

    public Set<Entry> buildTopEntries() throws IOException {
        Drive googleDriveService = GoogleDriveService.getDriveService();

        String nextPageToken = null;
        do {
            FileList result = googleDriveService.files().list()
                    .setPageSize(100)
                    .setQ("mimeType = '" + MIME_APPS_FOLDER + "' or mimeType = '" + MIME_IMAGE + "'")
                    .setFields("nextPageToken, files(id, mimeType, name, webViewLink, parents)")
                    //.setFields("nextPageToken, files")
                    .setPageToken(nextPageToken)
                    .execute();

            nextPageToken = result.getNextPageToken();
            List<com.google.api.services.drive.model.File> files = result.getFiles();
            if (files == null || files.size() == 0) {
                logger.warn("No files found.");
            } else {
                for (com.google.api.services.drive.model.File file : files) {
                    if (file.getParents() == null) {
                        logger.warn("skipping (parents=null)");
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
                parentFolder = addEntry((Folder) topEntry, entry);
            }

            if (entry.getId().equals(topEntry.getParentId())) {
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
        if (parent.getId().equals(entry.getParentId())) {
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

    public static void main(String[] args) throws Exception {
        GooglePhotoSync googlePhotoSync = new GooglePhotoSync();
        Set<Entry> topEntries = googlePhotoSync.buildTopEntries();

        Folder classifiedAds = null;
        for (Entry topEntry : topEntries) {
            if (topEntry instanceof Folder && topEntry.getName().equals("classified_ads")) {
                classifiedAds = (Folder) topEntry;
                break;
            }
        }

        if (classifiedAds == null)
            throw new Exception("Can't find classified_ads");

        logger.debug("{}\n{}", classifiedAds.getClass(), classifiedAds);

        createFiles(new File(BASE_DIR), classifiedAds);
    }

    private static void createFiles(File baseDir, Folder folder) {
        if (!baseDir.isDirectory())
            throw new IllegalArgumentException("baseDir is not a directory");

        for (Entry entry : folder.getChildren()) {
            if (entry instanceof Folder)
                createFiles(baseDir, (Folder) entry);
            else
                logger.debug("{}, {}", entry.getJavaPackage("classified_ads"), entry.getName());
        }
    }

}
