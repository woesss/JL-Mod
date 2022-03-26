/*
 *  Copyright 2022 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.playsoftware.j2meloader.donations;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import ru.playsoftware.j2meloader.R;

public class DonationsFragment extends Fragment {

	public static DonationsFragment newInstance() {
		return new DonationsFragment();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_donations, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		// PayPal
		ViewStub paypalViewStub = view.findViewById(R.id.donations_paypal_stub_me);
		paypalViewStub.inflate();

		Button btPayPal = view.findViewById(org.sufficientlysecure.donations.R.id.donations__paypal_donate_button);
		btPayPal.setOnClickListener(v -> donateOnClick("https://www.paypal.me/j2meforever"));

		// Qiwi
		ViewStub qiwiViewStub = view.findViewById(R.id.donations_qiwi_stub_me);
		qiwiViewStub.inflate();

		Button btQiwi = view.findViewById(org.sufficientlysecure.donations.R.id.donations__paypal_donate_button);
		btQiwi.setOnClickListener(v -> donateOnClick("https://my.qiwi.com/Yuryi-Khk_7vnCWvd"));

		// PayPal
		ViewStub paypalViewStub2 = view.findViewById(R.id.donations_paypal_stub_jl);
		paypalViewStub2.inflate();

		Button btPayPal2 = view.findViewById(org.sufficientlysecure.donations.R.id.donations__paypal_donate_button);
		btPayPal2.setOnClickListener(v -> donateOnClick("https://www.paypal.me/nikita36078"));

		// Yandex
		ViewStub ymViewStub = view.findViewById(R.id.donations_ym_stub_jl);
		ymViewStub.inflate();

		Button btYm = view.findViewById(org.sufficientlysecure.donations.R.id.donations__paypal_donate_button);
		btYm.setOnClickListener(v -> donateOnClick("https://money.yandex.ru/to/41001670387745"));
	}

	public void donateOnClick(String uri) {
		Uri payPalUri = Uri.parse(uri);
		Intent intent = new Intent(Intent.ACTION_VIEW, payPalUri);

		try {
			startActivity(intent);
			requireActivity().finish();
		} catch (Exception e) {
			e.printStackTrace();
			openDialog(org.sufficientlysecure.donations.R.string.donations__alert_dialog_title,
					getString(org.sufficientlysecure.donations.R.string.donations__alert_dialog_no_browser));
		}
	}

	void openDialog(int title, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(requireActivity());
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setCancelable(true);
		dialog.setPositiveButton(org.sufficientlysecure.donations.R.string.donations__button_close, null);
		dialog.show();
	}
}
