package com.group1.team.autodiary;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageRecognitionRequest {

    public static final int REQUEST_LABEL = 0, REQUEST_FACE = 1;

    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private Context mContext;

    public ImageRecognitionRequest(Context context) {
        this.mContext = context;
    }

    public static EntityAnnotation getLabel(BatchAnnotateImagesResponse response) {
        if (response == null)
            return null;

        List<AnnotateImageResponse> responses = response.getResponses();
        if (!response.isEmpty()) {
            List<EntityAnnotation> annotations = responses.get(0).getLabelAnnotations();
            if (annotations != null && !annotations.isEmpty())
                return annotations.get(0);
        }
        return null;
    }

    public static FaceAnnotation getFace(BatchAnnotateImagesResponse response) {
        if (response == null)
            return null;

        List<AnnotateImageResponse> responses = response.getResponses();
        if (!response.isEmpty()) {
            List<FaceAnnotation> annotations = responses.get(0).getFaceAnnotations();
            if (annotations != null && !annotations.isEmpty())
                return annotations.get(0);
        }
        return null;
    }

    public BatchAnnotateImagesResponse request(Bitmap bitmap, int request) {
        try {
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            VisionRequestInitializer requestInitializer = new VisionRequestInitializer(mContext.getString(R.string.visionKey)) {
                @Override
                protected void initializeVisionRequest(VisionRequest<?> visionRequest) throws IOException {
                    super.initializeVisionRequest(visionRequest);
                    String packageName = mContext.getPackageName();
                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);
                    String sig = PackageManagerUtils.getSignature(mContext.getPackageManager(), packageName);
                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                }
            };

            Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
            builder.setVisionRequestInitializer(requestInitializer);

            Vision vision = builder.build();

            BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
            batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                Image base64EncodedImage = new Image();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                base64EncodedImage.encodeContent(imageBytes);
                annotateImageRequest.setImage(base64EncodedImage);

                annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                    Feature labelDetection = new Feature();
                    labelDetection.setType(request == REQUEST_LABEL ? "LABEL_DETECTION" : "FACE_DETECTION");
                    labelDetection.setMaxResults(10);
                    add(labelDetection);
                }});
                add(annotateImageRequest);
            }});

            Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
            annotateRequest.setDisableGZipContent(true);

            return annotateRequest.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
