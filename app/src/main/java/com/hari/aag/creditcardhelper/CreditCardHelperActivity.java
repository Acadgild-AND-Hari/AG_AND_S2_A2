package com.hari.aag.creditcardhelper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hari.aag.creditcardhelper.R.id.cardFinalBalance;

public class CreditCardHelperActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String LOG_TAG = CreditCardHelperActivity.class.getSimpleName();
    private static final String PREFS_NAME = CreditCardHelperActivity.class.getSimpleName();

    private String cardBalanceStr, cardYearlyInterestRateStr, cardMinimumPaymentStr;
    private String cardFinalBalanceStr, cardMonthsRemainingStr, cardInterestPaidStr;

    private static final String CARD_BALANCE = "cardBalance";
    private static final String YEARLY_INTEREST_RATE = "cardYearlyInterestRate";
    private static final String MINIMUM_PAYMENT = "cardMinimumPayment";
    private static final String FINAL_BALANCE = "cardFinalBalance";
    private static final String MONTHS_REMAINING = "cardMonthsRemaining";
    private static final String INTEREST_PAID = "cardInterestPaid";

    @BindView(R.id.cardBalance) EditText cardBalanceET;
    @BindView(R.id.cardYearlyInterestRate) EditText cardYearlyInterestRateET;
    @BindView(R.id.cardMinimumPayment) EditText cardMinimumPaymentET;
    @BindView(cardFinalBalance) EditText cardFinalBalanceET;
    @BindView(R.id.cardMonthsRemaining) EditText cardMonthsRemainingET;
    @BindView(R.id.cardInterestPaid) EditText cardInterestPaidET;

    @BindView(R.id.compute) Button computeBtn;
    @BindView(R.id.clear) Button clearBtn;

    @BindString(R.string.default_final_balance_value) String defaultFinalBalanceValue;
    @BindString(R.string.default_months_remaining_value) String defaultMonthsRemainingValue;
    @BindString(R.string.default_interet_paid_value) String defaultInterestPaidValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_helper);
        ButterKnife.bind(this);

        computeBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);

        readValuesFromPrefs();
        updateValueToUI(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.compute:
                double principle_P, minDue_D, rateOfInterest_R, newPrinciple_P;
                double monthlyInterestAmount_MIA, availableBalance_AB;
                double finalCardBalance_FCB = 0, totalInterestPaid_TIP = 0;
                int noOfMonths_N = 0;

                String cardBalanceStr1 = cardBalanceET.getText() + "";
                if (cardBalanceStr1.isEmpty()){
                    Toast.makeText(this, "Card Balance is Empty!", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Card Balance is Empty!");
                    break;
                }

                String cardYearlyInterestRateStr1 = cardYearlyInterestRateET.getText() + "";
                if (cardYearlyInterestRateStr1.isEmpty()){
                    Toast.makeText(this, "Card Yearly Interest Rate is Empty!", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Card Yearly Interest Rate is Empty!");
                    break;
                }

                String cardMinimumPaymentStr1 = cardMinimumPaymentET.getText() + "";
                if (cardMinimumPaymentStr1.isEmpty()){
                    Toast.makeText(this, "Card Minimum Payment is Empty!", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Card Minimum Payment is Empty!");
                    break;
                }

                if ((!cardBalanceStr.isEmpty() && cardBalanceStr.equals(cardBalanceStr1) ||
                        (!cardYearlyInterestRateStr.isEmpty() && cardYearlyInterestRateStr.equals(cardYearlyInterestRateStr1)) ||
                        (!cardMinimumPaymentStr.isEmpty() && cardMinimumPaymentStr.equals(cardMinimumPaymentStr1)))){
                    Log.d(LOG_TAG, "Calculation Skipped!");
                    break;
                }

                cardBalanceStr = cardBalanceStr1;
                cardYearlyInterestRateStr = cardYearlyInterestRateStr1;
                cardMinimumPaymentStr = cardMinimumPaymentStr1;

                principle_P = Double.parseDouble(cardBalanceStr1);
                rateOfInterest_R = Double.parseDouble(cardYearlyInterestRateStr1);
                minDue_D = Double.parseDouble(cardMinimumPaymentStr1);

                newPrinciple_P = principle_P;
                do {
                    monthlyInterestAmount_MIA = ((newPrinciple_P * rateOfInterest_R) / (100 * 12));

                    availableBalance_AB = newPrinciple_P - minDue_D + monthlyInterestAmount_MIA;

                    if (noOfMonths_N == 0)
                        finalCardBalance_FCB = availableBalance_AB;

                    totalInterestPaid_TIP += monthlyInterestAmount_MIA;

                    noOfMonths_N++;

                    newPrinciple_P = availableBalance_AB;
                } while (availableBalance_AB > 0);

                cardFinalBalanceStr = getFormattedDouble(finalCardBalance_FCB);
                cardMonthsRemainingStr = noOfMonths_N + "";
                cardInterestPaidStr = getFormattedDouble(totalInterestPaid_TIP);

                updateValueToUI(false);
                saveValuesToPrefs();
                break;
            case R.id.clear:
                cardBalanceStr = "";
                cardYearlyInterestRateStr = "";
                cardMinimumPaymentStr = "";
                cardFinalBalanceStr = defaultFinalBalanceValue;
                cardMonthsRemainingStr = defaultMonthsRemainingValue;
                cardInterestPaidStr = defaultInterestPaidValue;

                updateValueToUI(true);
                saveValuesToPrefs();
                break;
        }
    }

    private String getFormattedDouble(Double value){
        return String.format("%.2f", value);
    }

    private void updateValueToUI(boolean isInputFieldsReq){
        if (isInputFieldsReq){
            cardBalanceET.setText(cardBalanceStr);
            cardYearlyInterestRateET.setText(cardYearlyInterestRateStr);
            cardMinimumPaymentET.setText(cardMinimumPaymentStr);
        }
        cardFinalBalanceET.setText(cardFinalBalanceStr);
        cardMonthsRemainingET.setText(cardMonthsRemainingStr);
        cardInterestPaidET.setText(cardInterestPaidStr);
    }

    private void readValuesFromPrefs(){
        SharedPreferences mySharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        cardBalanceStr = mySharedPrefs.getString(CARD_BALANCE, "");
        cardYearlyInterestRateStr = mySharedPrefs.getString(YEARLY_INTEREST_RATE, "");
        cardMinimumPaymentStr = mySharedPrefs.getString(MINIMUM_PAYMENT, "");
        cardFinalBalanceStr = mySharedPrefs.getString(FINAL_BALANCE, defaultFinalBalanceValue);
        cardMonthsRemainingStr = mySharedPrefs.getString(MONTHS_REMAINING, defaultMonthsRemainingValue);
        cardInterestPaidStr = mySharedPrefs.getString(INTEREST_PAID, defaultInterestPaidValue);

        Log.d(LOG_TAG, "Values Read from Prefs.");
        dumpPrefValues();
    }

    private void saveValuesToPrefs(){
        SharedPreferences.Editor prefsEditor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        prefsEditor.putString(CARD_BALANCE, cardBalanceStr);
        prefsEditor.putString(YEARLY_INTEREST_RATE, cardYearlyInterestRateStr);
        prefsEditor.putString(MINIMUM_PAYMENT, cardMonthsRemainingStr);
        prefsEditor.putString(FINAL_BALANCE, cardFinalBalanceStr);
        prefsEditor.putString(MONTHS_REMAINING, cardMonthsRemainingStr);
        prefsEditor.putString(INTEREST_PAID, cardInterestPaidStr);
        prefsEditor.commit();

        Log.d(LOG_TAG, "Values Saved to Prefs.");
        dumpPrefValues();
    }

    private void dumpPrefValues(){
        Log.d(LOG_TAG, CARD_BALANCE + " - " + cardBalanceStr);
        Log.d(LOG_TAG, YEARLY_INTEREST_RATE +  " - " + cardYearlyInterestRateStr);
        Log.d(LOG_TAG, MINIMUM_PAYMENT +  " - " + cardMinimumPaymentStr);
        Log.d(LOG_TAG, FINAL_BALANCE +  " - " + cardFinalBalanceStr);
        Log.d(LOG_TAG, MONTHS_REMAINING +  " - " + cardMonthsRemainingStr);
        Log.d(LOG_TAG, INTEREST_PAID +  " - " + cardInterestPaidStr);
    }

}
