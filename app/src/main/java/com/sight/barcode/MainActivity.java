package com.sight.barcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    //permissões
    private String[] permissoes = new String[]{
            Manifest.permission.CAMERA
    };

    private PreviewView previewView;
    private ListenableFuture cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private MyImageAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**validar permissões*/
        Permissoes.validarPermissoes(permissoes, this, 1);
        previewView = findViewById(R.id.previewView);
        //this.getWindow().setFlags(1024, 1024);
        setCameraProviderListener();
        analyzer = new MyImageAnalyzer(getSupportFragmentManager());
        //analyzer = new MyImageAnalyzer();
    }

    private void setCameraProviderListener() {
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(MainActivity.this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                    bindPreview(processCameraProvider);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(MainActivity.this));
    }

    public void bindPreview(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        //preview.setSurfaceProvider(previewView.getSurfaceProvider());
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1200, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);
        cameraProvider.unbindAll();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    //teste

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        private FragmentManager fragmentManager;
        private bottom_dialog bd;

        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            bd = new bottom_dialog();
        }

        public MyImageAnalyzer() {;
        }

        @Override
        public void analyze(ImageProxy imageProxy) {
            // Pass image to an ML Kit Vision API
            //Log.d("Sandbox", "### Would analyze the image here ...");
            scanbarcode(imageProxy);
            imageProxy.close();
            //scanbarcode(imageProxy);
        }


        private void scanbarcode(ImageProxy imageProxy) {

            @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
            //@SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (image != null) {
                //assert image != null;
                InputImage inputImage = InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees());
                BarcodeScannerOptions options =
                        new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                        Barcode.FORMAT_QR_CODE,
                                        Barcode.FORMAT_EAN_13)
                                .build();

                //BarcodeScanner scanner = BarcodeScanning.getClient(options);
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            // Task completed successfully
                            readerBarcodeData(barcodes);
                            image.close();
                        })
                        .addOnFailureListener(Throwable::printStackTrace)
                        .addOnCompleteListener(task -> image.close());
            }

        }

        private String nomeFormatoCodigo(int tipo){
            String formato = "";
            switch (tipo){
                case Barcode.FORMAT_ALL_FORMATS:
                    formato = "FORMAT_ALL_FORMATS";
                    break;
                case Barcode.FORMAT_EAN_13:
                    formato = "FORMAT_EAN_13";
                    break;
                case Barcode.FORMAT_QR_CODE:
                    formato = "FORMAT_QR_CODE";
                    break;
                case Barcode.FORMAT_AZTEC:
                    formato = "FORMAT_AZTEC";
                    break;
                case Barcode.FORMAT_CODE_39:
                    formato = "FORMAT_CODE_39";
                    break;
                case Barcode.FORMAT_CODE_93:
                    formato = "FORMAT_CODE_93";
                    break;
                case Barcode.FORMAT_CODE_128:
                    formato = "FORMAT_CODE_128";
                    break;
                case Barcode.FORMAT_DATA_MATRIX:
                    formato = "FORMAT_DATA_MATRIX";
                    break;
                case Barcode.FORMAT_EAN_8:
                    formato = "FORMAT_EAN_8";
                    break;
                case Barcode.FORMAT_PDF417:
                    formato = "FORMAT_PDF417";
                    break;
                case Barcode.FORMAT_UPC_A:
                    formato = "FORMAT_UPC_A";
                    break;
                case Barcode.FORMAT_UPC_E:
                    formato = "FORMAT_UPC_E";
                    break;
                case Barcode.FORMAT_ITF:
                    formato = "FORMAT_ITF";
                    break;
                default:
                    formato = "FORMAT_UNKNOWN";
            }

            return formato;
        }

        private void readerBarcodeData(List<Barcode> barcodes) {
            for (Barcode barcode : barcodes) {
                String rawValue = barcode.getRawValue();
                int valueType = barcode.getValueType();
                // See API reference for complete list of supported types
                //Toast.makeText(MainActivity.this, String.valueOf(valueType), Toast.LENGTH_LONG).show();
                if (valueType == Barcode.TYPE_URL) {
                    if (!bd.isAdded()) {
                        bd.show(fragmentManager, "");
                    }
                    bd.fetchurl(barcode.getUrl().getUrl());
                    //Toast.makeText(MainActivity.this, rawValue, Toast.LENGTH_LONG).show();
                }else{
                    String nome = nomeFormatoCodigo(barcode.getFormat());
                    Toast.makeText(
                            MainActivity.this,
                            "Formato: " + nome + "\n" +
                                    "Valor: " + barcode.getRawValue(),
                            Toast.LENGTH_LONG).show();
                }
            }
            /*for (Barcode barcode: barcodes) {
                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();

                String rawValue = barcode.getRawValue();
                Toast.makeText(MainActivity.this, rawValue, Toast.LENGTH_LONG).show();
            }*/
        }

    }

    /**
     * Metodo que percorre permissões no manifest
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResult : grantResults) {
            if (permissaoResult == PackageManager.PERMISSION_DENIED) {
                alertaPermissaoNegada();
            }
        }
    }

    /**
     * Alert Dialoq da permissão da camera
     */
    private void alertaPermissaoNegada() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para que o geduxEstoque funcione corretamente é necessario aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}