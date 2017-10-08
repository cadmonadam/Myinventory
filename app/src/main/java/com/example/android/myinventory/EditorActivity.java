package com.example.android.myinventory;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myinventory.data.ProductContract.ProductEntry;

/**
 * Created by Adam Cadmon on 2017. 07. 09..
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    Uri imageUri;
    private ImageView mImage;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSupplierName;
    private EditText mSupplierEmail;
    private TextView mQuantityTextView;
    private Button mPlusButton;
    private Button mMinusButton;
    private Button mOrder;
    private TextView mImageText;
    private int mQuantity;
    private Uri mCurrentProductUri;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mImage = (ImageView) findViewById(R.id.product_picture);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierName = (EditText) findViewById(R.id.supplier_name);
        mSupplierEmail = (EditText) findViewById(R.id.supplier_email);
        mQuantityTextView = (TextView) findViewById(R.id.edit_quantity_text_view);
        mPlusButton = (Button) findViewById(R.id.button_plus);
        mMinusButton = (Button) findViewById(R.id.button_minus);
        mImageText = (TextView) findViewById(R.id.add_photo_text);
        mOrder = (Button) findViewById(R.id.button_order);

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_product_title));
            mImageText.setText(getString(R.string.add_photo));
            mSupplierName.setEnabled(true);
            mSupplierEmail.setEnabled(true);
            mOrder.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_product_title));
            mImageText.setText(getString(R.string.change_photo));
            mSupplierName.setEnabled(false);
            mSupplierEmail.setEnabled(false);
            mOrder.setVisibility(View.VISIBLE);
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText.setOnTouchListener(mOnTouchListener);
        mPriceEditText.setOnTouchListener(mOnTouchListener);
        mSupplierName.setOnTouchListener(mOnTouchListener);
        mSupplierEmail.setOnTouchListener(mOnTouchListener);
        mMinusButton.setOnTouchListener(mOnTouchListener);
        mPlusButton.setOnTouchListener(mOnTouchListener);
        mOrder.setOnTouchListener(mOnTouchListener);
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mProductHasChanged = true;
            }
        });
    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intentType));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectPicture)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                mImage.setImageURI(imageUri);
                mImage.invalidate();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // In case of unsaved changes a dialog box appears where the user can confirm his/hers intention.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // in case of discard
                        finish();
                    }
                };

        // Indicates that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // In case of Keep editing, dismiss is
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Making and showing an Alert Dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Placing menu items into the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // In cas of new product the Delete menu remains hidden.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User chooses a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Save
            case R.id.action_save:
                // Insert Product
                if (saveProduct()) {
                    // Returns to CatalogActivity
                    finish();
                }
                return true;
            // Choosing the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Choosing the Up  button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // In case of unsaved changes a dialog box appears where the user can confirm his/hers intention.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Choosing discard button, navigate back to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Pops up a dialog box which notifies the user about the unsaved changes.
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //If we need to resupply a certain product we can make a new order via e-mail
    public void order(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + mSupplierEmail.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order of " + mNameEditText.getText().toString().trim());
        String message = "Dear " + mSupplierName.getText().toString().trim() + ", \n We would like to place an order of " + mNameEditText.getText().toString().trim() + ", details below:";
        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }

    private boolean saveProduct() {

        boolean allFilledOut = false;

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierEmailString = mSupplierEmail.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString();

        // Checking whether it is a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                TextUtils.isEmpty(quantityString) &&
                imageUri == null) {

            allFilledOut = true;
            return allFilledOut;
        }

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.productNameReq), Toast.LENGTH_SHORT).show();
            return allFilledOut;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.productPriceReq), Toast.LENGTH_SHORT).show();
            return allFilledOut;
        }

        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.supplierNameReq), Toast.LENGTH_SHORT).show();
            return allFilledOut;
        }

        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);

        if (TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, getString(R.string.supplierEmailReq), Toast.LENGTH_SHORT).show();
            return allFilledOut;
        }

        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.quantityReq), Toast.LENGTH_SHORT).show();
            return allFilledOut;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        if (imageUri == null) {
            Toast.makeText(this, getString(R.string.productPicReq), Toast.LENGTH_SHORT).show();
            return allFilledOut;
        }

        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, imageUri.toString());

        // Checking if it is a new or existing product
        if (mCurrentProductUri == null) {
            // In case of a new product inserting it into the provider and
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            // A toast message appears depending on whether or not the insertion was successful.
            if (newUri == null) {

                Toast.makeText(this, getString(R.string.error_saving_prod),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.prod_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // In case of an EXISTING product updating the product with content URI
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            // A toast message appears depending on whether or not the insertion was successful.
            if (rowsAffected == 0) {

                Toast.makeText(this, getString(R.string.error_saving_prod),
                        Toast.LENGTH_SHORT).show();
            } else {

                if (mProductHasChanged) {
                    Toast.makeText(this, getString(R.string.prod_saved),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        allFilledOut = true;
        return allFilledOut;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User confirms his/hers intention by clicking on the Delete button
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User confirms his/hers intention by clicking on the Delete button

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            // Calling the ContentResolver to delete the product at the given content URI.

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // A toast message appears depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {

                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
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
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }


        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            int sNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int sEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);

            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String sName = cursor.getString(sNameColumnIndex);
            String sEmail = cursor.getString(sEmailColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            String imageUriString = cursor.getString(pictureColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mSupplierName.setText(sName);
            mSupplierEmail.setText(sEmail);
            mQuantityTextView.setText(Integer.toString(mQuantity));
            imageUri = Uri.parse(imageUriString);
            mImage.setImageURI(imageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierName.setText("");
        mSupplierEmail.setText("");
        mQuantityTextView.setText("");
    }

    //This is the increment method for the quantity.
    public void increment(View view) {
        mQuantity++;
        displayQuantity();
    }

    //This is the decrement method for the quantity.
    public void decrement(View view) {
        if (mQuantity == 0) {
            Toast.makeText(this, R.string.noLessQuantity, Toast.LENGTH_SHORT).show();
        } else {
            mQuantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQuantityTextView.setText(String.valueOf(mQuantity));
    }
}
