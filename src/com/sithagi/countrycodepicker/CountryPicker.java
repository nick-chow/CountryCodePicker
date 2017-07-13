package com.sithagi.countrycodepicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class CountryPicker extends DialogFragment implements
        Comparator<Country> {

    private EditText searchEditText;
    private ListView countryListView;

    private CountryListAdapter adapter;

    private List<Country> allCountriesList;

    private List<Country> selectedCountriesList;

    private CountryPickerListener listener;

    public void setListener(CountryPickerListener listener) {
        this.listener = listener;
    }

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public ListView getCountryListView() {
        return countryListView;
    }

    public static Currency getCurrencyCode(String countryCode) {
        try {
            return Currency.getInstance(new Locale("en", countryCode));
        } catch (Exception e) {

        }
        return null;
    }

    private List<Country> getAllCountries() {
        if (allCountriesList == null) {
            try {
                allCountriesList = new ArrayList<Country>();
                String[] supportedCurrencies = getResources().getStringArray(R.array.supported_currency);
                String allCountriesCode = readEncodedJsonString(getActivity());

                JSONArray countryArray = new JSONArray(allCountriesCode);

                for (int i = 0; i < supportedCurrencies.length; i++) {
//                    JSONObject jsonObject = countryArray.getJSONObject(i);
//                    String countryName = jsonObject.getString("name");
//                    String countryDialCode = jsonObject.getString("dial_code");
//                    String countryCode = jsonObject.getString("code");
                    String countryCode = supportedCurrencies[i];
                    try {
                        Locale locale = new Locale("en", countryCode);
                        Currency currency = Currency.getInstance(locale);
                        Log.i("locale", countryCode);

                        if (locale != null && currency != null) {
                            Country country = new Country();

                            country.setCode(countryCode);
                            country.setName(locale.getDisplayCountry(Locale.ENGLISH));
                            country.setDialCode("");
                            country.setCurrency(currency.getCurrencyCode());
                            country.setCurrencySymbol(currency.getSymbol());
                            allCountriesList.add(country);
                        }
                    } catch (Exception e) {
                        Log.i("exception", e.toString());
                    }
                }

                //add eu manually, cause eu is not in the locale
                Country country = new Country();

                country.setCode("EU");
                country.setName("European Union");
                country.setDialCode("");
                country.setCurrency("EUR");
                country.setCurrencySymbol("€");

                allCountriesList.add(country);

                Collections.sort(allCountriesList, this);

                selectedCountriesList = new ArrayList<Country>();
                selectedCountriesList.addAll(allCountriesList);

                // Return
                return allCountriesList;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String readEncodedJsonString(Context context)
            throws java.io.IOException {
        String base64 = context.getResources().getString(R.string.countries_code);
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data, "UTF-8");
    }

    /**
     * To support show as dialog
     *
     * @param dialogTitle
     * @return
     */
    public static CountryPicker newInstance(String dialogTitle) {
        CountryPicker picker = new CountryPicker();
        Bundle bundle = new Bundle();
        bundle.putString("dialogTitle", dialogTitle);
        picker.setArguments(bundle);
        return picker;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.country_picker, null);

        getAllCountries();

        Bundle args = getArguments();
        if (args != null) {
            String dialogTitle = args.getString("dialogTitle");
            getDialog().setTitle(dialogTitle);

            int width = getResources().getDimensionPixelSize(
                    R.dimen.cp_dialog_width);
            int height = getResources().getDimensionPixelSize(
                    R.dimen.cp_dialog_height);
            getDialog().getWindow().setLayout(width, height);
        }

        searchEditText = (EditText) view
                .findViewById(R.id.country_code_picker_search);
        countryListView = (ListView) view
                .findViewById(R.id.country_code_picker_listview);

        adapter = new CountryListAdapter(getActivity(), selectedCountriesList);
        countryListView.setAdapter(adapter);

        countryListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    Country country = selectedCountriesList.get(position);
                    listener.onSelectCountry(country.getName(),
                            country.getCode(), country.getDialCode(), country.getCurrencySymbol(), country.getCurrency());
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        return view;
    }

    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedCountriesList.clear();

        for (Country country : allCountriesList) {
            if (country.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase()) || country.getCurrency().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
                selectedCountriesList.add(country);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public int compare(Country lhs, Country rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }

}
