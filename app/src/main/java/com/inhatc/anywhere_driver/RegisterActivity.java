package com.inhatc.anywhere_driver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class RegisterActivity extends AppCompatActivity {


    // Hwi
    // initialize DB
    DatabaseReference mDatabase;

    // Hwi
    // initialize for Upload Image
    private Uri filePath;
    private ImageView ivPreview;
    String get_phone;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hwi
        // Button components
        Button DoRegister = (Button) findViewById(R.id.btnDoRegister);
        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        // Hwi
        // EditText components
        final EditText name = (EditText) findViewById(R.id.edtUserName);
        final EditText phone = (EditText) findViewById(R.id.edtUserPhone);
        final EditText birth = (EditText) findViewById(R.id.edtUserBirth);

        // Hwi
        // Image components
        ivPreview = (ImageView) findViewById(R.id.iv_preview);

        // Hwi
        // 'Register' ?????? ?????? ??? ????????? ???????????? ??????
        // "????????? ?????? ??? ??????????????????" ????????? ????????? ??????
        DoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hwi
                // Convert EditText to String
                String get_name= name.getText().toString();
                get_phone= phone.getText().toString();
                String get_birth= birth.getText().toString();

                // Send UserData to DB
                HashMap result = new HashMap<>();
                result.put("name", get_name);
                result.put("phone", get_phone);
                result.put("birth", get_birth);

                // Hwi
                // firebase ??????
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("user").push().setValue(result);
                uploadFile();
                // Hwi
                // Make Toast
                Toast.makeText(RegisterActivity.this, "I'll let you know after the administrator approves it.",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Hwi
        // Image Upload Button
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //???????????? ??????
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Choose Image."), 0);
            }
        });
    }

    // Hwi
    // Image Upload
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // request????????? 0?????? OK??? ???????????? data??? ????????? ?????? ?????????
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri ????????? Bitmap?????? ???????????? ImageView??? ?????? ?????????.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Hwi
    // upload the file
    private void uploadFile() {
        // ???????????? ????????? ????????? ??????
        if (filePath != null) {
            // ????????? ?????? Dialog ?????????
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            // Set Filename as YYYYMMHH
            SimpleDateFormat formatter = new SimpleDateFormat(get_phone+"yyyyMMHH_mmss");
            Date now = new Date();
            String filename = formatter.format(now) + ".png";
            // storage ????????? ?????? ???????????? ????????? ??????.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://anywhere-6df10.appspot.com/").child("images/" + filename);
            // ???????????????...
            storageRef.putFile(filePath)
                    //?????????
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //????????? ?????? Dialog ?????? ??????
                            //Toast.makeText(getApplicationContext(), "Upload Success!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    // ?????????
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //Toast.makeText(getApplicationContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    // ?????????
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            // dialog??? ???????????? ???????????? ????????? ??????
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please choose file first.", Toast.LENGTH_SHORT).show();
        }
    }
}













