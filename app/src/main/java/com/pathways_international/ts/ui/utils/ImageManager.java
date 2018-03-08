package com.pathways_international.ts.ui.utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by android-dev on 3/8/18.
 */

public class ImageManager {


    private static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;"
            + "AccountName=[ACCOUNT NAME];"
            + "AccountKey=[ACCOUNT KEY]";

    private static CloudBlobContainer getContainer() throws Exception {
        // Retrieve storage account from connection string
        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(STORAGE_CONNECTION_STRING);

        // Create the blob client
        CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference("tsimages");

        return container;
    }

    public static String uploadImage(InputStream image, int imageLength, String imageName) throws Exception {
        CloudBlobContainer container = getContainer();

        container.createIfNotExists();

        CloudBlockBlob blockBlob = container.getBlockBlobReference(imageName);
        blockBlob.upload(image, imageLength);

        return imageName;

    }

    public static void getImage(String name, OutputStream imageStream, long imageLength) throws Exception {
        CloudBlobContainer container = getContainer();

        CloudBlockBlob cloudBlockBlob = container.getBlockBlobReference(name);

        if (cloudBlockBlob.exists()) {
            cloudBlockBlob.downloadAttributes();

            imageLength = cloudBlockBlob.getProperties().getLength();

            cloudBlockBlob.download(imageStream);
        }
    }
}
