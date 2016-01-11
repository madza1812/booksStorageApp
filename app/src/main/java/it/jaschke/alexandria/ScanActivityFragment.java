package it.jaschke.alexandria;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ScanActivityFragment extends DialogFragment implements ZXingScannerView.ResultHandler {

    private final String TAG = this.getClass().getSimpleName();

    private ZXingScannerView mScannerView;

    public ScanActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getActivity());
        //return inflater.inflate(R.layout.fragment_scan, container, false);
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        /*Toast.makeText(getActivity(), "Contents = " + rawResult.getText() +
                ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
        mScannerView.startCamera();*/
        if (rawResult.getBarcodeFormat().toString().equals(AddBook.SCAN_RESULT_FORMAT)) {
            Log.v(TAG, "Scanned ISBN 13 successfully !");
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AddBook.SCAN_EAN_KEY, rawResult.getText());
            resultIntent.putExtra("FORMAT", rawResult.getBarcodeFormat().toString());
            getActivity().setResult(2, resultIntent);
            getActivity().finish();
            mScannerView.stopCamera();
        } else {
            Log.v(TAG, "Not a ISBN 13 format !");
            Toast.makeText(getActivity(), "Contents = " + rawResult.getText() +
                    ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
            mScannerView.startCamera();
            Util.displayNeutralAlert(getActivity(),
                    getString(R.string.format_error_title),
                    getString(R.string.format_error_message));
        }
    }
}
