package com.example.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
//import android.os.FileUtils;
import org.apache.commons.io.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_ITEM_TEXT = "item_txt";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items; //= new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
    Button btnAdd;
    EditText txtBox;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnAdd);
        txtBox = findViewById(R.id.txtBox);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                // Delete the item from the Model and Notify the adapter
                items.remove(position);
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };
        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single click at position" + position);
                //create the new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                //pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                //display the edited activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };
        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add item to model and notify adapter that an item has been inserted
                String todoItem = txtBox.getText().toString();
                items.add(todoItem);
                itemsAdapter.notifyItemInserted(items.size()-1);
                txtBox.setText("");
                Toast.makeText(getApplicationContext(), "Item Added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    //Handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // Delete if necessary
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            //Retrieve the updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            //Extract the original position of the editied item from the position key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);
            //update the model at the right position with new item text
            items.set(position, itemText);
            //notify the adapter
            itemsAdapter.notifyItemChanged(position);
            //Persist the changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Item Updated", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");

    }
    //Loads and reads data by reading all lines of the data file
    private void loadItems() {
        try{
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e){
            Log.e("MainActivity", "Error reading Items", e);
            items = new ArrayList<>();
        }
    }
    // Saves Data by writing onto the data file
    private  void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing Items", e);
        }
    }
}