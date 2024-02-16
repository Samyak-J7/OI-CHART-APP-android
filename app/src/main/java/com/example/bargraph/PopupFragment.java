    package com.example.bargraph;
    import android.app.AlertDialog;
    import android.app.Dialog;
    import android.content.DialogInterface;
    import android.content.res.Configuration;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.text.Html;
    import android.text.SpannableString;
    import android.text.style.AbsoluteSizeSpan;
    import android.util.DisplayMetrics;
    import android.util.Log;
    import android.util.TypedValue;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.TableLayout;
    import android.widget.TableRow;
    import android.widget.TextView;
    import androidx.fragment.app.DialogFragment;
    import org.json.JSONException;
    import org.json.JSONObject;
    import java.text.DecimalFormat;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Locale;
    import android.graphics.Typeface;
    public class PopupFragment extends DialogFragment {
        private static final String ARG_JSON_DATA = "json_data";
        private static final String ARG_MESSAGE2 = "message2";
        private static final String ARG_LOT ="";
        double lot_size=0;
        public static PopupFragment newInstance(String jsonData, String message2,  String lot) {
            PopupFragment fragment = new PopupFragment();
            Bundle args = new Bundle();
            args.putString(ARG_JSON_DATA, jsonData);
            args.putString(ARG_MESSAGE2, message2);
            args.putString(ARG_LOT,lot);
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.popup_layout, null);
            String jsonData = getArguments().getString(ARG_JSON_DATA);
            String message2 = getArguments().getString(ARG_MESSAGE2);
            String lot =      getArguments().getString(ARG_LOT);
            lot_size= Double.parseDouble(lot);
            try {
                JSONObject json = new JSONObject(jsonData);
                populateTable(view, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            message2="<font color='red'><b><u>"+message2+"</u></b>";
            SpannableString spannableString = new SpannableString(Html.fromHtml(message2));
            spannableString.setSpan(new AbsoluteSizeSpan(24, true), 0,5 , 0);
            builder.setView(view)
                    .setCustomTitle(new androidx.appcompat.widget.AppCompatTextView(getActivity()) {
                        {
                            setText(spannableString);
                            setGravity(Gravity.CENTER);
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            return builder.create();
        }
        private String getDayName(int dayNumber) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            String dayOfWeek = dateFormat.format(calendar.getTime());
            return dayOfWeek.substring(0, 3);
        }
        private void populateTable(View view, JSONObject json) throws JSONException {
            TableLayout tableLayout = view.findViewById(R.id.tableLayout);
            List<String> order=new ArrayList<>();
            int ddw = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            for(int i=ddw+1;i<7;i++){
                String dayName = getDayName(i);
                order.add(dayName);
            }
            for(int i=2;i<=ddw;i++){
                String dayName = getDayName(i);
                order.add(dayName);
            }
            JSONObject rearrangedJson = new JSONObject();
            for (String day : order) {
                if (json.has(day)) {
                    rearrangedJson.put(day, json.getJSONObject(day));
                }
            }
            int keyCount=0;
            Iterator<String> jkeys = json.keys();
            while (jkeys.hasNext()) {
                String day = jkeys.next();
                if (keyCount >= 5) {
                    rearrangedJson.put(day, json.getJSONObject(day));
                }
                keyCount++;}
            Iterator<String> keys = rearrangedJson.keys();
            Configuration configuration = getResources().getConfiguration();
            float fsize=  configuration.fontScale;
            Log.e("fsize", String.valueOf(fsize));
            float textSizeInSp=12;
            float scdep=8f;
            int dayfw=60;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidthInDp = displayMetrics.widthPixels;
            if (screenWidthInDp<1080){
                dayfw=60- (int) ((1080-screenWidthInDp)*0.05f);
                scdep=8+((1080-screenWidthInDp) * 0.0027f );
            }
            else if (screenWidthInDp>1080){
                dayfw=60+ (int) ((screenWidthInDp-1080)*0.05f);
                scdep=8-((screenWidthInDp-1080) * 0.0027f );
            }
            float siz= screenWidthInDp/scdep;
            int fixedwidth= (int) siz;

            TableRow headerRow = new TableRow(getActivity());

            TextView dayHeadingTextView = new TextView(getActivity());
            dayHeadingTextView.setText("DAY");
            dayHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            dayHeadingTextView.setGravity(Gravity.END);
            dayHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            dayHeadingTextView.setTypeface(null,Typeface.BOLD);
            setDynamicLayoutParams2(dayHeadingTextView, "COI".length(), dayfw);
            headerRow.addView(dayHeadingTextView);

            TextView callOIHeadingTextView = new TextView(getActivity());
            callOIHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            callOIHeadingTextView.setText("COI");
            callOIHeadingTextView.setGravity(Gravity.END);
            callOIHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            callOIHeadingTextView.setTypeface(null,Typeface.BOLD);
            setDynamicLayoutParams2(callOIHeadingTextView, "COI".length(), fixedwidth);
            headerRow.addView(callOIHeadingTextView);


            TextView rsHeadingTextView = new TextView(getActivity());
            rsHeadingTextView.setText("");
            rsHeadingTextView.setGravity(Gravity.END);
            rsHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            rsHeadingTextView.setTypeface(null,Typeface.BOLD);
            headerRow.addView(rsHeadingTextView);

            TextView callrsHeadingTextView = new TextView(getActivity());
            callrsHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            callrsHeadingTextView.setText("Rs");
            callrsHeadingTextView.setGravity(Gravity.LEFT);
            callrsHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            callrsHeadingTextView.setTypeface(null,Typeface.BOLD);
            headerRow.addView(callrsHeadingTextView);

            TextView putOIHeadingTextView = new TextView(getActivity());
            putOIHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            putOIHeadingTextView.setText("POI");
            putOIHeadingTextView.setGravity(Gravity.END);
            putOIHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            putOIHeadingTextView.setTypeface(null,Typeface.BOLD);
            setDynamicLayoutParams2(putOIHeadingTextView, "POI".length(), fixedwidth);
            headerRow.addView(putOIHeadingTextView);

            TextView prsHeadingTextView = new TextView(getActivity());
            prsHeadingTextView.setText("");
            prsHeadingTextView.setGravity(Gravity.END);
            prsHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            prsHeadingTextView.setTypeface(null,Typeface.BOLD);
            headerRow.addView(prsHeadingTextView);

            TextView putrsHeadingTextView = new TextView(getActivity());
            putrsHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            putrsHeadingTextView.setText("Rs");
            putrsHeadingTextView.setGravity(Gravity.LEFT);
            putrsHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            putrsHeadingTextView.setTypeface(null,Typeface.BOLD);
            headerRow.addView(putrsHeadingTextView);

            TextView micHeadingTextView = new TextView(getActivity());
            micHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            micHeadingTextView.setText("CM");
            micHeadingTextView.setGravity(Gravity.END);
            micHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            micHeadingTextView.setTypeface(null,Typeface.BOLD);
            setDynamicLayoutParams2(micHeadingTextView, "cm".length(), fixedwidth);
            headerRow.addView(micHeadingTextView);

            TextView mHeadingTextView = new TextView(getActivity());
            mHeadingTextView.setText(" - ");
            mHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            mHeadingTextView.setGravity(Gravity.END);
            mHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            mHeadingTextView.setTypeface(null,Typeface.BOLD);
            headerRow.addView(mHeadingTextView);

            TextView mipHeadingTextView = new TextView(getActivity());
            mipHeadingTextView.setText("PM");
            mipHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            mipHeadingTextView.setGravity(Gravity.END);
            mipHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            mipHeadingTextView.setTypeface(null,Typeface.BOLD);
            headerRow.addView(mipHeadingTextView);

            TextView pcrHeadingTextView = new TextView(getActivity());
            pcrHeadingTextView.setText("PCR");
            pcrHeadingTextView.setTextColor(Color.parseColor("#ffff00"));
            pcrHeadingTextView.setGravity(Gravity.END);
            pcrHeadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
            pcrHeadingTextView.setTypeface(null,Typeface.BOLD);
            setDynamicLayoutParams2(pcrHeadingTextView, "puts".length(), fixedwidth);
            headerRow.addView(pcrHeadingTextView);

            tableLayout.addView(headerRow);
            while (keys.hasNext()) {
                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                String day = keys.next();
                JSONObject dayData = json.getJSONObject(day);
                TableRow row = new TableRow(getActivity());

                TextView dayTextView = new TextView(getActivity());
                dayTextView.setText(day);
                dayTextView.setGravity(Gravity.RIGHT);
                dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                dayTextView.setTypeface(null, Typeface.BOLD);
                row.addView(dayTextView);

                TextView callOITextView = new TextView(getActivity());
                String call_oi= String.valueOf(dayData.getInt("call_oi"));
                callOITextView.setText(call_oi);
                callOITextView.setGravity(Gravity.RIGHT);
                callOITextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                callOITextView.setTypeface(null, Typeface.BOLD);
                row.addView(callOITextView);

                TextView r1TextView = new TextView(getActivity());
                String r1= " ₹";
                r1TextView.setText(r1);
                r1TextView.setGravity(Gravity.RIGHT);
                r1TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(r1TextView);

                TextView callrsTextView = new TextView(getActivity());
                String call_rs= String.valueOf(dayData.getInt("call_rs"));
                callrsTextView.setText(call_rs);
                callrsTextView.setGravity(Gravity.RIGHT);
                callrsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(callrsTextView);

                TextView putOITextView = new TextView(getActivity());
                String put_oi= String.valueOf(dayData.getInt("put_oi"));
                putOITextView.setText(put_oi);
                putOITextView.setGravity(Gravity.RIGHT);
                putOITextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                putOITextView.setTypeface(null, Typeface.BOLD);
                row.addView(putOITextView);

                TextView p1TextView = new TextView(getActivity());
                String p1= " ₹";
                p1TextView.setText(p1);
                p1TextView.setGravity(Gravity.RIGHT);
                p1TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(p1TextView);

                TextView putrsTextView = new TextView(getActivity());
                String put_rs= String.valueOf(dayData.getInt("put_rs"));
                putrsTextView.setText(put_rs);
                putrsTextView.setGravity(Gravity.RIGHT);
                putrsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(putrsTextView);


                TextView micTextView = new TextView(getActivity());
                double micValue = dayData.getDouble("call_rs") * dayData.getDouble("call_oi") * lot_size / 10000000;
                String mic = decimalFormat.format(micValue);
                if (!mic.contains(".")) {
                    mic += ".0";
                }
                micTextView.setText(mic);
                micTextView.setGravity(Gravity.RIGHT);
                micTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(micTextView);

                TextView m1TextView = new TextView(getActivity());
                String m1= " - ";
                m1TextView.setText(m1);
                m1TextView.setGravity(Gravity.RIGHT);
                m1TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(m1TextView);

                TextView mipTextView = new TextView(getActivity());
                double mipValue = dayData.getDouble("put_rs") * dayData.getDouble("put_oi") * lot_size / 10000000;
                String mip = decimalFormat.format(mipValue);
                if (!mip.contains(".")) {
                    mip += ".0";
                }
                mipTextView.setText(mip);
                mipTextView.setGravity(Gravity.RIGHT);
                mipTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                row.addView(mipTextView);

                String pcr= decimalFormat.format( dayData.getDouble("put_oi") / dayData.getDouble("call_oi") )+"x";
                if(pcr.equals("NaNx")){pcr="-";}
                TextView pcrTextView = new TextView(getActivity());
                pcrTextView.setText(pcr);
                pcrTextView.setGravity(Gravity.RIGHT);
                pcrTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                pcrTextView.setTypeface(null, Typeface.BOLD);
                row.addView(pcrTextView);


                setDynamicLayoutParams(dayTextView, day.length());
                setDynamicLayoutParams(callOITextView, call_oi.length());
                setDynamicLayoutParams(putOITextView, put_oi.length());
                setDynamicLayoutParams(micTextView, mic.length());
                setDynamicLayoutParams(pcrTextView, pcr.length());
                row.setBackgroundColor(Color.parseColor("#F5F5DC"));

                if (day.matches("B1[0-6]")) {
                    row.setBackgroundColor(Color.parseColor("#ffec3d"));
                }
                tableLayout.addView(row);
            }
        }
        private void setDynamicLayoutParams(TextView textView, int contentLength) {
            int minWidth = 15;
            int padding = 20;
            int width = minWidth + contentLength *12;
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(padding, 0, 0, 0);
            textView.setLayoutParams(layoutParams);
        }

        private void setDynamicLayoutParams2(TextView textView, int contentLength, int fixedWidth) {
            int padding = 20;
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(fixedWidth, TableRow.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(padding, 0, 0, 0);
            textView.setLayoutParams(layoutParams);
        }

    }
