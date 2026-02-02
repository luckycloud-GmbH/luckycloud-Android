package com.seafile.seadroid2.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.CertificateInfo;
import com.seafile.seadroid2.ssl.SSLTrustManager;
import com.seafile.seadroid2.ssl.SSLTrustManager.SslFailureReason;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;


public class SslConfirmDialog extends DialogFragment {
    public interface Listener {
        void onAccepted(boolean rememberChoice);
        void onRejected();
    }

    public static final String FRAGMENT_TAG = "SslConfirmDialog";
    public static final String DEBUG_TAG = "SslConfirmDialog";

    private Account account;
    private Listener listener;
    private X509Certificate certificate;
    private TextView messageText;
    private TextView commonNameText;
    // private TextView altSubjNamesText;
    private TextView sha256Text;
    private TextView sha1Text;
    private TextView md5Text;
    private TextView serialNumberText;
    private TextView notBeforeText;
    private TextView notAfterText;
    private CardView okCard;
    private TextView okText;
    private CardView cancelCard;
    private TextView cancelText;

    public SslConfirmDialog() {
    }

    @SuppressLint("ValidFragment")
    public SslConfirmDialog(Account account, Listener listener) {
        this.listener = listener;
        this.account = account;
    }

    @SuppressLint("ValidFragment")
    public SslConfirmDialog(Account account, X509Certificate certificate, Listener listener) {
        this.listener = listener;
        this.account = account;
        this.certificate = certificate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.dialog_ssl_confirm, null);

        messageText = (TextView)view.findViewById(R.id.message);
        commonNameText = (TextView) view.findViewById(R.id.common_name);
        // altSubjNamesText = (TextView) view.findViewById(R.id.alt_subj_name);
        sha256Text = (TextView) view.findViewById(R.id.sha256);
        sha1Text = (TextView) view.findViewById(R.id.sha1);
        md5Text = (TextView) view.findViewById(R.id.md5);
        serialNumberText = (TextView) view.findViewById(R.id.serial_number);
        notBeforeText = (TextView) view.findViewById(R.id.not_before);
        notAfterText = (TextView) view.findViewById(R.id.not_after);
        okCard = (CardView) view.findViewById(R.id.ok_card);
        okText = (TextView) view.findViewById(R.id.ok_text);
        cancelCard = (CardView) view.findViewById(R.id.cancel_card);
        cancelText = (TextView) view.findViewById(R.id.cancel_text);

        String host = null;

        try {
            host = new URL(account.server).getHost();
        } catch (MalformedURLException e) {
            // ignore
        }

        SslFailureReason reason = SSLTrustManager.instance().getFailureReason(account);
        X509Certificate cert = null;
        try {
            cert = SSLTrustManager.instance().getCertificateInfo(account);
        } catch (CertificateParsingException e) {
            e.printStackTrace();
        }
        String msg = "";
        if (reason == SslFailureReason.CERT_NOT_TRUSTED) {
            msg =getActivity().getString(R.string.ssl_confirm, host);
        } else {
            msg = getActivity().getString(R.string.ssl_confirm_cert_changed, host);
        }
        messageText.setText(msg);

        if (cert != null) {
            CertificateInfo certInfo = new CertificateInfo(cert);
            commonNameText.setText(certInfo.getSubjectName());
            // String[] subjAltNames = certInfo.getSubjectAltNames();
            // altSubjNamesText.setText((subjAltNames.length > 0) ? StringUtils.join(subjAltNames, ", ") : "—");
            sha256Text.setText(getActivity().getString(R.string.sha256, certInfo.getSignature("SHA-256")));
            sha1Text.setText(getActivity().getString(R.string.sha1, certInfo.getSignature("SHA-1")));
            md5Text.setText(getActivity().getString(R.string.md5, certInfo.getSignature("MD5")));
            serialNumberText.setText(getActivity().getString(R.string.serial_number, certInfo.getSerialNumber()));
            notBeforeText.setText(getActivity().getString(R.string.not_before, certInfo.getNotBefore().toLocaleString()));
            notAfterText.setText(getActivity().getString(R.string.not_after, certInfo.getNotAfter().toLocaleString()));
        } else if (certificate != null) {
            CertificateInfo certInfo = new CertificateInfo(certificate);
            commonNameText.setText(certInfo.getSubjectName());
            sha256Text.setText(getActivity().getString(R.string.sha256, certInfo.getSignature("SHA-256")));
            sha1Text.setText(getActivity().getString(R.string.sha1, certInfo.getSignature("SHA-1")));
            md5Text.setText(getActivity().getString(R.string.md5, certInfo.getSignature("MD5")));
            serialNumberText.setText(getActivity().getString(R.string.serial_number, certInfo.getSerialNumber()));
            notBeforeText.setText(getActivity().getString(R.string.not_before, certInfo.getNotBefore().toLocaleString()));
            notAfterText.setText(getActivity().getString(R.string.not_after, certInfo.getNotAfter().toLocaleString()));
        } else {
            String not_available = getActivity().getString(R.string.not_available);
            commonNameText.setText(not_available);
            sha256Text.setText(not_available);
            sha1Text.setText(not_available);
            md5Text.setText(not_available);
            serialNumberText.setText(not_available);
            notBeforeText.setText(not_available);
            notAfterText.setText(not_available);
        }

        okText.setText(R.string.yes);
        cancelText.setText(R.string.no);

        okCard.setOnClickListener(v -> {
            Log.d(DEBUG_TAG, "listener.onAccepted is called");
            listener.onAccepted(true);
            dismiss();
        });
        cancelCard.setOnClickListener(v -> {
            Log.d(DEBUG_TAG, "listener.onRejected is called");
            listener.onRejected();
            dismiss();
        });

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        onDialogCreated(dialog);

        return dialog;
    }

    protected void onDialogCreated(Dialog dialog) {
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(DEBUG_TAG, "listener.onRejected is called");
        listener.onRejected();
    }
}
