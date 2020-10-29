package com.paidapp.laughtercafe.ui.cart;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paidapp.laughtercafe.Adapter.MyCartAdapter;
import com.paidapp.laughtercafe.Callback.ILoadTimeFromFirebaseListener;
import com.paidapp.laughtercafe.Callback.ISearchCategoryCallbackListener;
import com.paidapp.laughtercafe.Common.Common;
import com.paidapp.laughtercafe.Common.MySwipeHelper;
import com.paidapp.laughtercafe.Database.CartDataSource;
import com.paidapp.laughtercafe.Database.CartDatabase;
import com.paidapp.laughtercafe.Database.CartItem;
import com.paidapp.laughtercafe.Database.LocalCartDataSource;
import com.paidapp.laughtercafe.EventBus.CounterCartEvent;
import com.paidapp.laughtercafe.EventBus.HideFABCart;
import com.paidapp.laughtercafe.EventBus.MenuItemBack;
import com.paidapp.laughtercafe.EventBus.UpdateItemInCart;
import com.paidapp.laughtercafe.Model.AddonModel;
import com.paidapp.laughtercafe.Model.CategoryModel;
import com.paidapp.laughtercafe.Model.FCMSendData;
import com.paidapp.laughtercafe.Model.FoodModel;
import com.paidapp.laughtercafe.Model.OrderModel;
import com.paidapp.laughtercafe.Model.SizeModel;
import com.paidapp.laughtercafe.R;
import com.paidapp.laughtercafe.Remote.ICloudFunctions;
import com.paidapp.laughtercafe.Remote.IFCMService;
import com.paidapp.laughtercafe.Remote.RetrofitICloudClient;
import com.paidapp.laughtercafe.Remote.RetrofitFCMClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener, ISearchCategoryCallbackListener, TextWatcher {

    private BottomSheetDialog addonBottomSheetDialog;
    private ChipGroup chip_group_addon,chip_group_user_selected_addon;
    private EditText edt_search;
    private ISearchCategoryCallbackListener searchFoodCallbackListener;
    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    private static final int REQUEST_BRAINTREE_CODE = 7777;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    private  MyCartAdapter adapter;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    String address,comment;

    ICloudFunctions cloudFunctions;
    IFCMService ifcmService;
    ILoadTimeFromFirebaseListener listener;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more step!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order,null);


        EditText edt_comment = (EditText)view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton)view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton)view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = (RadioButton)view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton)view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = (RadioButton)view.findViewById(R.id.rdi_braintree);

        places_fragment = (AutocompleteSupportFragment)getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                txt_address.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(), ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        txt_address.setText(Common.currentUser.getAddress());

        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
            {
                txt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.VISIBLE);
                places_fragment.setHint(Common.currentUser.getAddress());
            }
        });
        rdi_other_address.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
            {
                txt_address.setVisibility(View.VISIBLE);
            }
        });
        rdi_ship_to_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
            {
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);
                        })
                        .addOnCompleteListener(task -> {
                            String coordinates = new StringBuilder()
                                    .append(task.getResult().getLatitude())
                                    .append("/")
                                    .append(task.getResult().getLongitude()).toString();

                            Single <String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                                    task.getResult().getLongitude()));

                            Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>(){
                                @Override
                                public void onSuccess(String s) {
                                    txt_address.setText(s);
                                    txt_address.setVisibility(View.VISIBLE);
                                    places_fragment.setHint(s);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    txt_address.setText(e.getMessage());
                                    txt_address.setVisibility(View.VISIBLE);
                                }
                            });


                        });
            }
        });

        builder.setView(view);
        builder.setNegativeButton("NO", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("YES", (dialogInterface, which) -> {
           // Toast.makeText(getContext(), "Implement later", Toast.LENGTH_SHORT).show();
            if (rdi_cod.isChecked())
                paymentCOD(txt_address.getText().toString(),edt_comment.getText().toString());
            else if (rdi_braintree.isChecked())
            {
                address = txt_address.getText().toString();
                comment = edt_comment.getText().toString();
                if (!TextUtils.isEmpty(Common.currentToken))
                {
                    DropInRequest dropInRequest = new DropInRequest().clientToken(Common.currentToken);
                    startActivityForResult(dropInRequest.getIntent(getContext()),REQUEST_BRAINTREE_CODE);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            if (places_fragment != null)
                getActivity().getSupportFragmentManager()
                .beginTransaction().remove(places_fragment)
                .commit();
        });
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                    Common.currentRestaurant.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Double>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Double totalPrice) {
                            double finalPrice = totalPrice;
                            OrderModel order = new OrderModel();
                            order.setUserId(Common.currentUser.getUid());
                            order.setUserName(Common.currentUser.getName());
                            order.setUserPhone(Common.currentUser.getPhone());
                            order.setShippingAddress(address);
                            order.setComment(comment);

                            if (currentLocation != null)
                            {
                                order.setLat(currentLocation.getLatitude());
                                order.setLng(currentLocation.getLongitude());
                            }
                            else
                            {
                                order.setLat(-0.1f);
                                order.setLng(-0.1f);
                            }

                            order.setCartItemList(cartItems);
                            order.setTotalPayment(totalPrice);
                            order.setDiscount(0);
                            order.setFinalPayment(finalPrice);
                            order.setCod(true);
                            order.setTransactionId("Cash on Delivery");

                            syncLocalTimeWithGlobaltime(order);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!e.getMessage().contains("Query returned empty result set"))
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }, throwable -> {
            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void syncLocalTimeWithGlobaltime(OrderModel order) {
        final DatabaseReference offestRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long offset = dataSnapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis()+offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy HH:mm");
                Date resultDate = new Date(estimatedServerTimeMs);
                Log.d("TEST_DATE",""+sdf.format(resultDate));

                listener.onLoadTimeSuccess(order,estimatedServerTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoadTimeFailed(databaseError.getMessage());
            }
        });
    }

    private void writeOrderToFirebase(OrderModel order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    cartDataSource.cleanCart(Common.currentUser.getUid(),
                            Common.currentRestaurant.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    Map<String,String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE,"New Order");
                                    notiData.put(Common.NOTI_CONTENT,"You have new order from "+Common.currentUser.getPhone());
                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(),notiData);
                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        Toast.makeText(getContext(), "Order placed Successfully!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "Order was sent but failure to send notification!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }));

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result="";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
            if (addressList != null && addressList.size() > 0)
            {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            }
            else
                result = "Address not found";
        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    private Unbinder unbinder;

    private CartViewModel cartViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        cloudFunctions = RetrofitICloudClient.getInstance(Common.currentRestaurant.getPaymentUrl()).create(ICloudFunctions.class);
        listener = this;

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(this, cartItems -> {
            if (cartItems == null || cartItems.isEmpty())
            {
                recycler_cart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
            }
            else
            {
                recycler_cart.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                adapter = new MyCartAdapter(getContext(),cartItems);
                recycler_cart.setAdapter(adapter);
            }

        });
        unbinder = ButterKnife.bind(this,root);
        initViews();
        initLocation();
        return root;
    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void initViews() {
        searchFoodCallbackListener = this;
        initPlaceClient();
        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_cart,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Delete",30,0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItems(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            adapter.notifyItemRemoved(pos);
                                            sumAllItemInCart();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            Toast.makeText(getContext(), "Delete item from Cart successful !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#5D4037"),
                        pos -> {
                           CartItem cartItem = adapter.getItemAtPosition(pos);
                           FirebaseDatabase.getInstance()
                                   .getReference(Common.RESTAURANT_REF)
                                   .child(Common.currentRestaurant.getUid())
                                   .child(Common.CATEGORY_REF)
                                   .child(cartItem.getCategoryId())
                                   .addListenerForSingleValueEvent(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                           if (dataSnapshot.exists())
                                           {
                                               CategoryModel categoryModel = dataSnapshot.getValue(CategoryModel.class);
                                               searchFoodCallbackListener.onSearchCategoryFound(categoryModel,cartItem);

                                           }
                                           else
                                           {
                                               searchFoodCallbackListener.onSearchCategoryNotFound("Food not found");
                                           }
                                       }

                                       @Override
                                       public void onCancelled(@NonNull DatabaseError databaseError) {
                                           searchFoodCallbackListener.onSearchCategoryNotFound(databaseError.getMessage());
                                       }
                                   });
                        }));
            }
        };

        sumAllItemInCart();

        addonBottomSheetDialog = new BottomSheetDialog(getContext(),R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display,null);
        chip_group_addon = (ChipGroup)layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText)layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {
            displayUserSelectedAddon(chip_group_user_selected_addon);
            calculateTotalPrice();
        });

    }

    private void displayUserSelectedAddon(ChipGroup chip_group_user_selected_addon) {
        if (Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() > 0)
        {
            chip_group_user_selected_addon.removeAllViews();
            for (AddonModel addonModel:Common.selectedFood.getUserSelectedAddon())
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+₦")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                    {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_user_selected_addon.addView(chip);
            }
        }
        else
            chip_group_user_selected_addon.removeAllViews();
    }

    private void initPlaceClient() {
        Places.initialize(getContext(),getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());
    }


    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total: ₦").append(aDouble));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart)
        {
            cartDataSource.cleanCart(Common.currentUser.getUid(),
                    Common.currentRestaurant.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "Clear Cart Successful", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().postSticky(new CounterCartEvent(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event)
    {
        if (event.getCartItem() != null)
        {
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("Total: ₦")
                        .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BRAINTREE_CODE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();

                cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                        Common.currentRestaurant.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Double totalPrice) {
                                compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),
                                        Common.currentRestaurant.getUid())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(cartItems -> {

                                                    Map<String,String> headers = new HashMap<>();
                                                    headers.put("Authorization",Common.buildToken(Common.authorizeKey));

                                            compositeDisposable.add(cloudFunctions.submitPayment(
                                                    headers,
                                                    totalPrice,
                                                    nonce.getNonce())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(braintreeTranscation -> {
                                                        if (braintreeTranscation.isSuccess())
                                                        {
                                                            double finalPrice = totalPrice;
                                                            OrderModel order = new OrderModel();
                                                            order.setUserId(Common.currentUser.getUid());
                                                            order.setUserName(Common.currentUser.getName());
                                                            order.setUserPhone(Common.currentUser.getPhone());
                                                            order.setShippingAddress(address);
                                                            order.setComment(comment);

                                                            if (currentLocation != null)
                                                            {
                                                                order.setLat(currentLocation.getLatitude());
                                                                order.setLng(currentLocation.getLongitude());
                                                            }
                                                            else
                                                            {
                                                                order.setLat(-0.1f);
                                                                order.setLng(-0.1f);
                                                            }

                                                            order.setCartItemList(cartItems);
                                                            order.setTotalPayment(totalPrice);
                                                            order.setDiscount(0);
                                                            order.setFinalPayment(finalPrice);
                                                            order.setCod(false);
                                                            order.setTransactionId(braintreeTranscation.getTransaction().getId());

                                                            //writeOrderToFirebase(order);
                                                            syncLocalTimeWithGlobaltime(order);
                                                        }

                                                    }, throwable -> {
                                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                            );

                                        }, throwable -> {
                                            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        ));
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (!e.getMessage().contains("Query returned empty result set"))
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {

    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem) {
        FoodModel foodModel = Common.findFoodInListById(categoryModel,cartItem.getFoodId());
        if (foodModel != null)
        {
            showUpdateDialog(cartItem,foodModel);
        }
        else
            Toast.makeText(getContext(), "Food Id not found", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(CartItem cartItem, FoodModel foodModel) {
        Common.selectedFood = foodModel;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_update_cart,null);
        builder.setView(itemView);

        Button btn_ok = (Button)itemView.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button)itemView.findViewById(R.id.btn_cancel);

        RadioGroup rdi_group_size = (RadioGroup)itemView.findViewById(R.id.rdi_group_size);
        chip_group_user_selected_addon = (ChipGroup)itemView.findViewById(R.id.chip_group_user_selected_addon);
        ImageView img_add_on = (ImageView)itemView.findViewById(R.id.img_add_addon);
        img_add_on.setOnClickListener(v -> {
            if (foodModel.getAddon() != null)
            {
                displayAddonList();
                addonBottomSheetDialog.show();
            }
        });

        if (foodModel.getSize() != null) {
            for (SizeModel sizeModel : foodModel.getSize()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                        Common.selectedFood.setUserSelectedSize(sizeModel);
                    calculateTotalPrice();
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);
            }

            if (rdi_group_size.getChildCount() > 0)
            {
                RadioButton radioButton = (RadioButton)rdi_group_size.getChildAt(0);
                radioButton.setChecked(true);
            }
        }

        displayAlreadySelectedAddon(chip_group_user_selected_addon,cartItem);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_ok.setOnClickListener(view -> {
            cartDataSource.deleteCartItems(cartItem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            if (Common.selectedFood.getUserSelectedAddon() != null)
                                cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
                            else
                                cartItem.setFoodAddon("Default");
                            if (Common.selectedFood.getUserSelectedSize() != null)
                                cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
                            else
                                cartItem.setFoodSize("Default");
                            cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),
                                    Common.selectedFood.getUserSelectedAddon()));

                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(() -> {
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                calculateTotalPrice();
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), "Update Cart Success!!", Toast.LENGTH_SHORT).show();
                                            },throwable -> {
                                                Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            })

                            );
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btn_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    private void displayAlreadySelectedAddon(ChipGroup chip_group_user_selected_addon, CartItem cartItem) {
        if (cartItem.getFoodAddon() != null && !cartItem.getFoodAddon().equals("Default"))
        {
            List<AddonModel> addonModels = new Gson().fromJson(
                    cartItem.getFoodAddon(),new TypeToken<List<AddonModel>>(){}.getType());
           Common.selectedFood.setUserSelectedAddon(addonModels);
           chip_group_user_selected_addon.removeAllViews();

            for (AddonModel addonModel:addonModels)
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+₦")
                        .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_selected_addon.addView(chip);
            }
        }
    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon() != null && Common.selectedFood.getAddon().size() > 0)
        {
            chip_group_addon.clearCheck();
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            for (AddonModel addonModel:Common.selectedFood.getAddon())
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+₦")
                .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                    {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void onSearchCategoryNotFound(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();
        for (AddonModel addonModel:Common.selectedFood.getAddon())
        {
            if (addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+₦")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                    {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}