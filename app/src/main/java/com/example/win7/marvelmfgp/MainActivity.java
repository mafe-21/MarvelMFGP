package com.example.win7.marvelmfgp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static com.example.win7.marvelmfgp.R.id.txtInput;

public class MainActivity extends AppCompatActivity {

    //private Retrofit retrofit;
    EditText txtEquation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        txtEquation = (EditText) findViewById(R.id.txtInput);
        Button btnSave = (Button) findViewById(R.id.btnSend);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Calculate
                String input = txtEquation.getText().toString();
                String result = input;
                boolean right = true;

                HashMap<Integer, type> formula_type = new HashMap<Integer, type>();
                HashMap<Integer, String> formula_expression = new HashMap<Integer, String>();
                HashMap<Integer, Integer> formula_result = new HashMap<Integer, Integer>();
                HashMap<Integer, Integer> formula_index = new HashMap<Integer, Integer>();
                HashMap<Integer, Integer> formula_index_inv = new HashMap<Integer, Integer>();

                int count_left_parenthesis = 0;
                List<Integer> index_left_parenthesis = new ArrayList<Integer>();

                int count_right_parenthesis = 0;
                List<Integer> index_right_parenthesis = new ArrayList<Integer>();

                int count_operators = 0;
                int[] index_operators;

                int count_numbers = 0;

                int count_spaces = 0;
                int[] index_spaces;

                type actual_type = type.NONE;
                type before_type = type.NONE;

                int count_same_types = 0;

                while (right)
                {
                    for (int i = 0; i < result.length();i++)
                    {
                        if (result.substring(i,i) == "(")
                        {
                            count_left_parenthesis++;
                            index_left_parenthesis.add(i);
                            actual_type = type.LEFT_PARENTHESIS;
                        }
                        else if (result.substring(i,i) == ")")
                        {
                            count_right_parenthesis++;
                            index_right_parenthesis.add(i);
                            actual_type = type.RIGHT_PARENTHESIS;
                        }
                        else if (isOperator(result.substring(i,i).charAt(0)))
                        {
                            count_operators++;
                            actual_type = type.OPERATOR;
                        }
                        else if (isNumber(result.substring(i,i).charAt(0)))
                        {
                            count_numbers++;
                            actual_type = type.NUMBER;
                        }
                        else if (result.substring(i,i) == " ")
                        {
                            count_spaces++;

                            if (i + 1 < result.length() && i-1 > 0)
                            {
                                String nextChar = result.substring(i+1,i+1);

                                if (isNumber(nextChar.charAt(0)) || nextChar == "(")
                                {
                                    int countTwoOperatorsBefore = count_operators_before(result, i);

                                    if (countTwoOperatorsBefore > 2)
                                    {
                                        right = false;
                                        break;
                                    }
                                }
                            }

                            actual_type = type.SPACE;
                        }
                        else
                        {
                            right = false;
                            break;
                        }

                        if (actual_type == before_type)
                        {
                            count_same_types++;
                        }
                        else if (before_type == type.NONE)
                        {
                            count_same_types++;
                        }
                        else
                        {
                            formula_type.put(i - count_same_types, before_type);
                            formula_expression.put(i - count_same_types, result.substring(i - count_same_types, i));
                            formula_result.put(i - count_same_types, 0);
                            formula_index.put(i - count_same_types, formula_index.size());
                            formula_index_inv.put(formula_index_inv.size(), i - count_same_types);
                            count_same_types = 1;
                        }

                        before_type = actual_type;
                    }

                    if (count_left_parenthesis != count_right_parenthesis)
                    {
                        right = false;
                        break;
                    }

                    if (count_left_parenthesis!=0)
                    {
                        String expression1 = "";
                        String sub_expression1 = "";

                        for (int k = 1; k <=  count_left_parenthesis; k++)
                        {
                            int last_left_p = index_left_parenthesis.get(index_left_parenthesis.size()-1);
                            int first_right_p = index_right_parenthesis.get(k - 1);

                            if (last_left_p >= first_right_p)
                            {
                                right = false;
                                break;
                            }

                            expression1 = result.substring(last_left_p, first_right_p);
                            int index_exp1 = 0;

                            if (expression1.contains("*"))
                            {
                                //reduce expression resolving first multiplication and division operations
                                index_exp1 = expression1.indexOf("*") + last_left_p;
                                double number1 = 0; //Operations with integer numbers
                                double number2 = 0;
                                int key1 = 0;
                                int key2 = 0;
                                double result_expresion1 = 0;

                                if (formula_index.containsKey(index_exp1))
                                {
                                    if (formula_index.containsValue(index_exp1 - 1) && formula_index.containsValue(index_exp1 + 1))
                                    {
                                        key1 = formula_index_inv.get(index_exp1 - 1);
                                        key2 = formula_index_inv.get(index_exp1 + 1);

                                        try
                                        {
                                            number1 = Double.parseDouble(formula_expression.get(key1));
                                            number2 = Double.parseDouble(formula_expression.get(key2));
                                        }
                                        catch (Exception ex)
                                        {
                                            right = false;
                                            break;
                                        }

                                        result_expresion1 = number1*number2;

                                        expression1 = expression1.substring(last_left_p,key1-last_left_p) + result_expresion1 + expression1.substring(key2-last_left_p, first_right_p);

                                        formula_expression.remove(index_exp1);
                                        formula_expression.remove(index_exp1 + 1);
                                        formula_type.remove(index_exp1);
                                        formula_type.remove(index_exp1 + 1);
                                        formula_result.remove(index_exp1);
                                        formula_result.remove(index_exp1 + 1);
                                        formula_index.remove(index_exp1);
                                        formula_index.remove(index_exp1 + 1);

                                        formula_expression.put(index_exp1, String.valueOf(result_expresion1));

                                    }
                                }
                                else
                                {
                                    right = false;
                                    break;
                                }

                            }
//                            if (expression1.contains("/"))
//                            {
//                                //reduce expression resolving first multiplication and division operations
//                                index_exp1 = expression1.indexOf("/");
//                            }

//                            for (int l = last_left_p + 1; l < first_right_p; l++)
//                            {
//
//                                if (formula.containsKey(l))
//                                {
//                                    if (formula.get(l) == type.NUMBER)
//                                }
//                            }

                        }
                    }





                }
            }
        });

//        retrofit = new Retrofit.Builder()
//                .baseUrl("https://gateway.marvel.com/v1/public/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOperator(Character character)
    {
        if (character == '+' || character == '-' ||
                character == '/' || character == '*')
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isNumber(Character character)
    {
        if (Character.isDigit(character))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int count_operators_before(String result, int i)
    {
        int countTwoOperatorsBefore = 0;

        for (int j = i - 1; j == 0 ; j--)
        {
            if(isOperator(result.substring(j,j).charAt(0)))
            {
                countTwoOperatorsBefore++;
            }
            else if (result.substring(j,j)== " ")
            {

            }
            else
            {
                break;
            }
        }

        return countTwoOperatorsBefore;
    }

    public enum type {
        NONE, OPERATOR, SPACE, NUMBER, LEFT_PARENTHESIS, RIGHT_PARENTHESIS
    }


}
