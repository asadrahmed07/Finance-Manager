package com.example.financemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashBoardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public static DashBoardFragment newInstance(String param1, String param2) {
        DashBoardFragment fragment = new DashBoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // Floating Button
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;

    // Floating button Textview..
    private TextView fab_income_txt;
    private TextView fab_expense_txt;

    private boolean isOpen = false;

    //Animation.
    private Animation FadOpen,FadClose;

    private TextView totalIncomeResult;
    private TextView totalExpenseResult;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    //Recycler view
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;

    private int totalexpense = 0;
    private  int totalsum = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview= inflater.inflate(R.layout.fragment_dash_board, container, false);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid= ((FirebaseUser) mUser).getUid();

        mIncomeDatabase= FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase=FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase").child(uid);

        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);

        // Connect floating button to layout

        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn= myview.findViewById(R.id.income_Ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_Ft_btn);

        //Connect floating text

        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt= myview.findViewById(R.id.expense_ft_text);

        //Total income and expense result set..
        totalIncomeResult = myview.findViewById(R.id.income_set_result);
        totalExpenseResult = myview.findViewById(R.id.expense_set_result);

        //Recycler
        mRecyclerIncome = myview.findViewById(R.id.recycler_income);
        mRecyclerExpense = myview.findViewById(R.id.recycler_expense);


        // Animation

        FadOpen = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_open);
        FadClose = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_close);


        fab_main_btn.setOnClickListener(view -> {

            addData();

         if(isOpen) {
             fab_income_btn.startAnimation(FadClose);
             fab_expense_btn.startAnimation(FadClose);
             fab_income_btn.setClickable(false);
             fab_expense_btn.setClickable(false);

             fab_income_txt.startAnimation(FadClose);
             fab_expense_txt.startAnimation(FadClose);
             fab_income_txt.setClickable(false);
             fab_expense_txt.setClickable(false);
             isOpen=false;
         }
         else {
             fab_income_btn.startAnimation(FadOpen);
             fab_expense_btn.startAnimation(FadOpen);
             fab_income_btn.setClickable(true);
             fab_expense_btn.setClickable(true);

             fab_income_txt.startAnimation(FadOpen);
             fab_expense_txt.startAnimation(FadOpen);
             fab_income_txt.setClickable(true);
             fab_expense_txt.setClickable(true);
             isOpen=true;
         }
        });

        // Calculate total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                totalsum = 0;
                for(DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    totalsum += data.getAmount();

                    String stResult = String.valueOf(totalsum);

                    totalIncomeResult.setText(stResult+".00");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Calculate total Expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalexpense = 0;
                for(DataSnapshot mysnapshot : snapshot.getChildren()) {

                    Data data = mysnapshot.getValue(Data.class);
                    totalexpense += data.getAmount();
                    String strTotalSum = String.valueOf(totalsum -totalexpense);
                    totalExpenseResult.setText(strTotalSum+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Recycler
        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);

        return myview;
    }

    //Floating button animation

    private void ftAnimation() {
        if(isOpen) {
            fab_income_btn.startAnimation(FadClose);
            fab_expense_btn.startAnimation(FadClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadClose);
            fab_expense_txt.startAnimation(FadClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);
            isOpen=false;
        }
        else {
            fab_income_btn.startAnimation(FadOpen);
            fab_expense_btn.startAnimation(FadOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadOpen);
            fab_expense_txt.startAnimation(FadOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);
            isOpen=true;
        }
    }

    private void addData() {
        //Fab button income..

        fab_income_btn.setOnClickListener(view -> incomeDataInsert());

        fab_expense_btn.setOnClickListener(view -> expenseDataInsert());
    }

    public void incomeDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_laryout_for_insertdata, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        final EditText edtAmount = myview.findViewById(R.id.ammount_edt);
        final EditText edtType = myview.findViewById(R.id.type_edt);
        final EditText edtNote = myview.findViewById(R.id.note_edt);

        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCancel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String type = edtType.getText().toString().trim();
            String ammount = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            if (TextUtils.isEmpty(type)) {
                edtType.setError("Mandatory");
                return;
            }

            if (TextUtils.isEmpty(ammount)) {
                edtAmount.setError("Mandatory");
                return;
            }

            int ourammontint = Integer.parseInt(ammount);

//            if (TextUtils.isEmpty(note)) {
//                edtNote.setError("Required Field..");
//                return;
//            }

            String id = mIncomeDatabase.push().getKey();
            String mDate = DateFormat.getDateInstance().format(new Date());

            Data data = new Data(ourammontint, type, note, id, mDate);
            mIncomeDatabase.child(id).setValue(data);

            Toast.makeText(getActivity(), "Data ADDED", Toast.LENGTH_SHORT).show();

            ftAnimation();
            dialog.dismiss();
        });


        btnCancel.setOnClickListener(v -> {
            ftAnimation();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void expenseDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_laryout_for_insertdata, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        EditText ammount = myview.findViewById(R.id.ammount_edt);
        EditText type = myview.findViewById(R.id.type_edt);
        EditText note = myview.findViewById(R.id.note_edt);

        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCancel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String tmAmmount = ammount.getText().toString().trim();
            String tmtype = type.getText().toString().trim();
            String tmnote = note.getText().toString().trim();

            if (TextUtils.isEmpty(tmAmmount)) {
                ammount.setError("Mandatory");
                return;
            }

            int inamount = Integer.parseInt(tmAmmount);

            if (TextUtils.isEmpty(tmtype)) {
                type.setError("Mandatory");
                return;
            }
//            if (TextUtils.isEmpty(tmnote)) {
//                note.setError("Requires Fields...");
//                return;
//            }

            String id = mExpenseDatabase.push().getKey();
            String mDate = DateFormat.getDateInstance().format(new Date());

            Data data = new Data(inamount, tmtype, tmnote, id, mDate);
            mExpenseDatabase.child(id).setValue(data);
            Toast.makeText(getActivity(), "Data added", Toast.LENGTH_SHORT).show();

            ftAnimation();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            ftAnimation();
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onStart() {

        super.onStart();

        fetch();

    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance().getReference().child("IncomeDatabase");
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mIncomeDatabase, Data.class).build();

        FirebaseRecyclerAdapter <Data, IncomeViewHolder> incomeAdapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {

                holder.setIncomeAmount(model.getAmount());
                holder.setIncomeType(model.getType());
                holder.setIncomeDate(model.getDate());

            }

            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income,parent,false);
                return new IncomeViewHolder(view);
            }
        };
       mRecyclerIncome.setAdapter(incomeAdapter);
       incomeAdapter.startListening();

        Query query1 = FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase");
        FirebaseRecyclerOptions<Data> option = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class).build();

        FirebaseRecyclerAdapter <Data, ExpenseViewHolder> expenseAdapter= new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(option) {
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {

                holder.setExpenseAmount(model.getAmount());
                holder.setExpenseType(model.getType());
                holder.setExpenseDate(model.getDate());

            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense,parent,false);
                return new ExpenseViewHolder(view);
            }
        };

        mRecyclerExpense.setAdapter(expenseAdapter);
        expenseAdapter.startListening();
    }

    //for income data

  public static class IncomeViewHolder extends RecyclerView.ViewHolder {

        View mIncomeview;
        public IncomeViewHolder (View itemView) {
            super(itemView);
            mIncomeview= itemView;
        }

        public void setIncomeType(String type) {
            TextView mtype = mIncomeview.findViewById(R.id.type_Income_ds);
            mtype.setText(type);

        }
        public void setIncomeAmount(int amount) {
            TextView mAmount = mIncomeview.findViewById(R.id.amount_income_ds);
            String strAmount = String.valueOf(amount);
            mAmount.setText(strAmount);
        }
        public void setIncomeDate (String date){
            TextView mDate = mIncomeview.findViewById(R.id.date_income_ds);
            mDate.setText(date);
        }
    }

   public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        View mExpenseView;
        public ExpenseViewHolder(View itemView) {
            super(itemView);
            mExpenseView=itemView;
        }
        public void setExpenseType(String type) {
            TextView mtype = mExpenseView.findViewById(R.id.type_Expense_ds);
            mtype.setText(type);
        }
        public void setExpenseAmount(int amount) {
            TextView mAmount = mExpenseView.findViewById(R.id.amount_expense_ds);
            String strAmount = String.valueOf(amount);
            mAmount.setText(strAmount);
        }
        public void setExpenseDate(String date) {
            TextView mDate = mExpenseView.findViewById(R.id.date_expense_ds);
            mDate.setText(date);
        }
    }

}