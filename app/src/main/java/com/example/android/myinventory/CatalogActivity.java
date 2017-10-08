package com.example.android.myinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.myinventory.data.ProductContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;
    View emptyView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Allocating a new intent to fab button in order to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
        mLayoutManager = new LinearLayoutManager(CatalogActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        emptyView = findViewById(R.id.empty_view);

        mCursorAdapter = new ProductCursorAdapter(this, null);
        mRecyclerView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    public void onProductClick(long id) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
        intent.setData(currentProductUri);

        startActivity(intent);
    }

    public void onBuyNowClick(long id, int quantity) {
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
        Log.v("CatalogActivity", "Uri: " + currentProductUri);
        quantity--;
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        int rowsEffected = getContentResolver().update(currentProductUri, values, null, null);
    }

    private void insertProduct() {

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummyDataName));
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, getString(R.string.dummyDataPrice));
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 0);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, getString(R.string.dummyDataPicUri));
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, getString(R.string.dummyDataSupplierName));
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, getString(R.string.dummyDataSupplierEmail));

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        Log.v("CatalogActivity", "Uri of new product: " + uri);

    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from product database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;

            case R.id.action_delete_all_products:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL};

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
