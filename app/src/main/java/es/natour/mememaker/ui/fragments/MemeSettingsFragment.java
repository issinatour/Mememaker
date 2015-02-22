package es.natour.mememaker.ui.fragments;


        import android.os.Bundle;
        import android.preference.PreferenceFragment;

        import es.natour.mememaker.R;


/**
 */
public class MemeSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
