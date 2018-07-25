package shay.example.com.dart_client.helper_classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class ImageUtility {

    private static Bitmap bitmap;

    public static Bitmap convertToBitmap(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convertToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }



    public static Bitmap getImage(){
        return bitmap;
    }
    public static void setImage(Bitmap bitmap){
        ImageUtility.bitmap = bitmap;
    }


}
