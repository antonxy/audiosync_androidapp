package org.audiosync;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class NumberChooser extends LinearLayout {

	private EditText numberField;

	public NumberChooser(Context context, AttributeSet attrs) {
		super(context, attrs);
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.number_chooser, this, true);
		
		numberField = (EditText)findViewById(R.id.numberField);
		((Button) findViewById(R.id.plusButton)).setOnClickListener(new PlusListener(numberField));
		((Button) findViewById(R.id.minusButton)).setOnClickListener(new MinusListener(numberField));
		((Button) findViewById(R.id.oneButton)).setOnClickListener(new OneListener(numberField));
		
	}
	
	public NumberChooser(Context context){
		this(context, null);
	}
	
	public int getNumber() throws NumberFormatException{
		return Integer.parseInt(numberField.getText().toString());
	}

}
