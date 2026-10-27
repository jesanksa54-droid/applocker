package com.example.applocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    static final String PREF = "locker_settings";
    LinearLayout root;
    String savedPin;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        savedPin = getSharedPreferences(PREF, 0).getString("pin", "");
        build();
    }

    @Override protected void onResume() { super.onResume(); build(); }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density); }

    private void build() {
        savedPin = getSharedPreferences(PREF, 0).getString("pin", "");

        ScrollView sv = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(Color.rgb(247, 244, 255));
        sv.addView(root);
        setContentView(sv);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(20), dp(20), dp(20));
        GradientDrawable hBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{ Color.rgb(54, 39, 94), Color.rgb(103, 80, 164) });
        hBg.setCornerRadius(dp(20));
        header.setBackground(hBg);

        TextView t1 = new TextView(this);
        t1.setText("🔐  App Locker");
        t1.setTextSize(26); t1.setTypeface(null, Typeface.BOLD);
        t1.setTextColor(Color.WHITE);
        header.addView(t1);

        TextView t2 = new TextView(this);
        t2.setText("আপনার পিন ও ফিঙ্গারপ্রিন্ট নিরাপত্তা");
        t2.setTextSize(14); t2.setTextColor(Color.rgb(230, 222, 250));
        t2.setPadding(0, dp(6), 0, 0);
        header.addView(t2);

        root.addView(header);

        if (savedPin.isEmpty()) { showPinSetup(); return; }

        boolean fpOn = getSharedPreferences(PREF, 0).getBoolean("fingerprint", false);

        LinearLayout status = card();
        TextView st = new TextView(this);
        st.setText("সেটিংস স্ট্যাটাস");
        st.setTextSize(14); st.setTextColor(Color.rgb(120, 110, 150));
        status.addView(st);

        TextView line3 = new TextView(this);
        line3.setText("👆  ফিঙ্গারপ্রিন্ট: " + (fpOn ? "চালু" : "বন্ধ"));
        line3.setTextSize(16); line3.setTextColor(Color.rgb(35, 30, 55));
        line3.setPadding(0, dp(8), 0, 0);
        status.addView(line3);

        root.addView(status);

        LinearLayout menu = card();
        menu.addView(menuItem("🔑  PIN পরিবর্তন", this::changePin));
        menu.addView(menuItem("👆  ফিঙ্গারপ্রিন্ট " + (fpOn ? "বন্ধ করুন" : "চালু করুন"),
                this::toggleFingerprint));
        menu.addView(menuItem("🔓  ফিঙ্গারপ্রিন্ট টেস্ট করুন", this::testFingerprint));
        root.addView(menu);

        LinearLayout info = card();
        TextView it = new TextView(this);
        it.setText("ℹ️  আপনার অ্যাপে এখন একটি সুরক্ষিত PIN এবং ফিঙ্গারপ্রিন্ট সিস্টেম তৈরি হয়েছে। আপনি চাইলে এই পিনটি আপনার ব্যক্তিগত ফাইল বা নোট সুরক্ষিত রাখতে ব্যবহার করতে পারেন।");
        it.setTextSize(14); it.setTextColor(Color.rgb(80, 75, 100));
        it.setLineSpacing(dp(3), 1f);
        info.addView(it);
        root.addView(info);
    }

    private void showPinSetup() {
        LinearLayout c = card();
        TextView t = new TextView(this);
        t.setText("শুরু করতে ৪–৮ সংখ্যার PIN সেট করুন");
        t.setTextSize(16); t.setTextColor(Color.rgb(35, 30, 55));
        c.addView(t);

        EditText pin = new EditText(this);
        pin.setHint("নতুন PIN (৪–৮ সংখ্যা)");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        c.addView(pin);

        EditText pin2 = new EditText(this);
        pin2.setHint("আবার লিখুন");
        pin2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        c.addView(pin2);

        c.addView(button("PIN সেট করুন", () -> {
            String p1 = pin.getText().toString();
            String p2 = pin2.getText().toString();
            if (p1.length() < 4 || p1.length() > 8) { msg("PIN ৪–৮ সংখ্যার হতে হবে"); return; }
            if (!p1.equals(p2)) { msg("দুইবার একই PIN দিন"); return; }
            getSharedPreferences(PREF, 0).edit().putString("pin", p1).apply();
            savedPin = p1;
            msg("PIN সেট হয়েছে!");
            build();
        }));
        root.addView(c);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(18));
        c.setBackground(bg);
        c.setElevation(dp(2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private Button button(String text, Runnable r) {
        Button b = new Button(this);
        b.setText(text); b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(103, 80, 164));
        bg.setCornerRadius(dp(14));
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        lp.topMargin = dp(10);
        b.setLayoutParams(lp);
        b.setOnClickListener(v -> r.run());
        return b;
    }

    private LinearLayout menuItem(String text, Runnable r) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(16), 0, dp(16));
        row.setClickable(true);
        row.setOnClickListener(v -> r.run());

        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16);
        t.setTextColor(Color.rgb(35, 30, 55));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(t, tlp);

        TextView arrow = new TextView(this);
        arrow.setText("›"); arrow.setTextSize(22);
        arrow.setTextColor(Color.rgb(160, 150, 190));
        row.addView(arrow);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(240, 235, 250));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        dlp.topMargin = dp(2);
        divider.setLayoutParams(dlp);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(row);
        wrap.addView(divider);
        return wrap;
    }

    private void toggleFingerprint() {
        boolean fpOn = getSharedPreferences(PREF, 0).getBoolean("fingerprint", false);
        getSharedPreferences(PREF, 0).edit().putBoolean("fingerprint", !fpOn).apply();
        msg(fpOn ? "ফিঙ্গারপ্রিন্ট বন্ধ হয়েছে" : "ফিঙ্গারপ্রিন্ট চালু হয়েছে");
        build();
    }

    private void testFingerprint() {
        if (Build.VERSION.SDK_INT < 28) { msg("আপনার ফোনে এটি সাপোর্ট করে না।"); return; }
        try {
            BiometricPrompt prompt = new BiometricPrompt.Builder(this)
                .setTitle("ফিঙ্গারপ্রিন্ট যাচাই").setSubtitle("আপনার আঙুল রাখুন")
                .setNegativeButton("বাতিল", getMainExecutor(), (d, w) -> {}).build();
            prompt.authenticate(new CancellationSignal(), getMainExecutor(),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult r) {
                        runOnUiThread(() -> msg("✅ ফিঙ্গারপ্রিন্ট সফল হয়েছে!"));
                    }
                    @Override public void onAuthenticationFailed() {
                        runOnUiThread(() -> msg("❌ ফিঙ্গারপ্রিন্ট মেলেনি।"));
                    }
                });
        } catch (Exception e) {
            msg("ফিঙ্গারপ্রিন্ট ব্যবহার করা যাচ্ছে না।");
        }
    }

    private void changePin() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), 0);

        EditText oldPin = new EditText(this);
        oldPin.setHint("বর্তমান PIN");
        oldPin.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addView(oldPin);

        EditText neu = new EditText(this);
        neu.setHint("নতুন PIN (৪–৮ সংখ্যা)");
        neu.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addView(neu);

        new AlertDialog.Builder(this)
            .setTitle("PIN পরিবর্তন")
            .setView(box)
            .setNegativeButton("বাতিল", null)
            .setPositiveButton("সেভ", (d, w) -> {
                if (!oldPin.getText().toString().equals(savedPin)) {
                    msg("বর্তমান PIN সঠিক নয়"); return;
                }
                String p = neu.getText().toString();
                if (p.length() < 4 || p.length() > 8) {
                    msg("নতুন PIN ৪–৮ সংখ্যার হতে হবে"); return;
                }
                savedPin = p;
                getSharedPreferences(PREF, 0).edit().putString("pin", p).apply();
                msg("PIN পরিবর্তন হয়েছে");
            }).show();
    }

    private void msg(String s) {
        new AlertDialog.Builder(this)
            .setMessage(s)
            .setPositiveButton("ঠিক আছে", null).show();
    }
          }
