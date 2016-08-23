package user.qrticketvalidator;

import android.app.Activity;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by SUCHANDRA on 23/8/16.
 */
public class MainActivity extends Activity {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    public ImageView resultview;
    private TextView barcodeInfo;
    public String idonly,noafterscan,reverse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView)findViewById(R.id.camera_view);
        barcodeInfo = (TextView)findViewById(R.id.code_info);
        resultview=(ImageView)findViewById(R.id.resultimage);

        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {

                            String result=(barcodes.valueAt(0).displayValue);

                            noafterscan = result.substring(5, 7)+ result.substring(12, 14)+result.substring(19, 21)+
                                    result.substring(26, 28)+result.substring(33, 35) ;
                            Log.e("values code",result);

                            reverse = new StringBuffer(noafterscan).
                                    reverse().toString();


                            //does the value by pasing congnitive reverense via params


                            Toast.makeText(getBaseContext(),reverse,Toast.LENGTH_LONG).show();
          Log.e("no after scan",reverse);
                            idonly = reverse.substring(0, Math.min(reverse.length(), 2));
                            Log.e("reverseid",idonly);

                            //                        Toast.makeText(getBaseContext(),idonly,Toast.LENGTH_LONG).show();
                            barcodeInfo.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue);
                            validate();
                        }
                    });
                }
            }
        });

    }

    private void validate() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,"http://suchandra2009.esy.es/Validateticket.php" ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        //Showing toast message of the response

                        if(s.contains("null")){

                            resultview .setBackgroundResource(R.drawable.invalid);


                        }
                        else  if(s.contains("valid")){

                            resultview .setBackgroundResource(R.drawable.valid);

                        }
                        else if(s.contains("")){
                            Toast.makeText(getBaseContext(),"issues detecting",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {


                        //Showing toast
                        Toast.makeText(MainActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();


                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String


                //Getting Image Name

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("idonly",idonly);
                Log.e("addeded param",idonly);


                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }
}
