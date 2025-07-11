package net.ifmain.androiddummy.onnx.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import net.ifmain.androiddummy.onnx.model.AnimeGANModel

/**
 * AndroidDummy
 * Class : AnimeGANViewModel.
 * Created by gayoung.
 * Created On 2025-07-11.
 * Description:
 */
class AnimeGANViewModel: ViewModel() {
    private var animeGANModel: AnimeGANModel? = null

    fun initializeModel(context: Context) {
        animeGANModel = AnimeGANModel(context).apply {
            initialize()
        }
    }

    override fun onCleared() {
        super.onCleared()
        animeGANModel?.close()
    }
}