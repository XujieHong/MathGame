package com.speakin.recorder.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.speakin.recorder.R;
import com.speakin.recorder.utils.Book;

/**
 * Created by hongxujie on 1/18/18.
 */


public class MathPkActivity extends Activity {

    public static MathPkActivity instance = null;

    private static final int buttonCount = 10;
    private TextView mathFormula;
    private TextView mathAnswer;
    private TextView mathFormulaHistory;
    private Button startButton;
    private MathProblems mp = new MathProblems();
    private int answer = 0;
    private String mMathFormula = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.math_pk);

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(startListener);

        mathFormula = findViewById(R.id.formula);
        mathAnswer = findViewById(R.id.answer);
        mathFormulaHistory = findViewById(R.id.history);

        int startID = R.id.num0;
        for (int i = 0; i < buttonCount; i++){
            findViewById(startID + i).setOnClickListener(numButtonListener);
        }
        findViewById(R.id.clear).setOnClickListener(numButtonListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    View.OnClickListener startListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            mMathFormula = mp.createMathFormula();
            mathFormula.setText(mMathFormula);

            sendBook();
        }
    };

    View.OnClickListener numButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            String strAnswer = "";

            switch(view.getId()) {
                case R.id.clear:
                    answer = 0;
                    break;
                default:
                    if(answer < 10){
                        answer = answer * 10 + view.getId() - R.id.num0;
                    }
                    break;
            }

            if(view.getId() == R.id.clear) {
                strAnswer = "";
            }else {
                strAnswer = "" + answer;
            }

            mathAnswer.setText(strAnswer);
            if (answer == mp.getAnswer()){
                String history = mMathFormula + answer;
                mathFormulaHistory.setText(history);
                mMathFormula = mp.createMathFormula();
                mathFormula.setText(mMathFormula);
                answer = 0;
                mathAnswer.setText("");
                sendBook();
            }
        }
    };

    private void sendBook(){
        Book book = new Book();
        book.setX(mp.getX());
        book.setY(mp.getY());
        book.setZ(mp.getZ());
        book.setFormula(mp.getFormula());
        MainActivity.instance.sendBook(book);
    }

    public void onBookReceived(Book book){
        mMathFormula = book.getFormula();
        mp.setMathFormula(book.getX(), book.getY(), book.getZ(), mMathFormula);

        mathFormula.setText(mMathFormula);
    }
}
