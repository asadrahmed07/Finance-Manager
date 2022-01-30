package com.example.financemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;


public class ExpenseFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    // Recycler View
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;


    //Text View
    private TextView expenseSumResult;

    //Edt data Item
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    private Button btnUpdate;
    private Button btnDelete;

     //Data variable.
    private String type;
    private String note;
    private int amount;
    private String post_key;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myview = inflater.inflate(R.layout.fragment_expense, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase").child(uid);

        recyclerView = myview.findViewById(R.id.recycler_id_expense);

        expenseSumResult = myview.findViewById(R.id.expense_txt_result);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int expenseSum = 0;
                for (DataSnapshot mysnapshot : snapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    expenseSum += data.getAmount();
                    String stTotalvalue = String.valueOf(expenseSum);
                    expenseSumResult.setText(stTotalvalue+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return myview;

    }

    public void onStart() {
        super.onStart();

        fetch();

    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase");
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class).build();

        adapter = new FirebaseRecyclerAdapter<Data, IncomeFragment.MyViewHolder>(options) {

            public IncomeFragment.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false);
                return new IncomeFragment.MyViewHolder(view);
            }

            protected void onBindViewHolder(IncomeFragment.MyViewHolder holder, int position, @NonNull Data model) {
                holder.setAmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(holder.getAdapterPosition()).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        updateDataItem();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MyViewHolder(View itemView) {
            super(itemView);
        }

        private void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }
        private void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }
        private void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);
        }
        private void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.ammount_txt_income);
            String stramount = String.valueOf(amount);

            mAmount.setText(stramount);
        }
    }

    private void updateDataItem() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.update_data_item,null);
        mydialog.setView(myview);

        edtAmount = myview.findViewById(R.id.ammount_edt);
        edtNote = myview.findViewById(R.id.note_edt);
        edtType = myview.findViewById(R.id.type_edt);

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());


        btnUpdate = myview.findViewById(R.id.btn_upd_Update);
        btnDelete = myview.findViewById(R.id.btnuPD_Delete);

        AlertDialog dialog = mydialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();
                String stamount = String.valueOf(amount);

                stamount = edtAmount.getText().toString().trim();

                int intamount = Integer.parseInt(stamount);
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(intamount,type,note,post_key,mDate);
                mExpenseDatabase.child(post_key).setValue(data);

                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mExpenseDatabase.child(post_key).removeValue();
            dialog.dismiss();
            }
        });

        dialog.show();

    }
}