package com.example.iqromandarin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.iqromandarin.database.AppDatabase
import com.example.iqromandarin.databinding.FragmentJilidBinding
import com.example.iqromandarin.model.Halaman
import com.example.iqromandarin.viewmodel.MainViewModel
import com.example.iqromandarin.viewmodel.MainViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class JilidFragment : Fragment() {

    private var _binding: FragmentJilidBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getDatabase(requireContext()))
    }

    private var jilidId: Int = 1

    companion object {
        private const val ARG_JILID_ID = "jilid_id"
        fun newInstance(jilidId: Int): JilidFragment {
            return JilidFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_JILID_ID, jilidId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jilidId = arguments?.getInt(ARG_JILID_ID) ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJilidBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getHalamanByJilid(jilidId).observe(viewLifecycleOwner) { halamanList ->
            if (halamanList.isNotEmpty()) {
                setupViewPager(halamanList)
            }
        }

        viewModel.getJilidById(jilidId).observe(viewLifecycleOwner) { jilid ->
            jilid?.let {
                binding.tvJilidDesc.text = it.deskripsi
            }
        }
    }

    private fun setupViewPager(halamanList: List<Halaman>) {
        val pagerAdapter = HalamanPagerAdapter(this, halamanList, jilidId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = "Hal ${position + 1}"
        }.attach()

        // Navigate to last unlocked halaman
        viewModel.getLastUnlockedHalaman(jilidId).observe(viewLifecycleOwner) { lastPage ->
            val targetPage = minOf(lastPage, halamanList.size - 1)
            if (targetPage >= 0) {
                binding.viewPager.setCurrentItem(targetPage, false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Pager Adapter for Halaman
    inner class HalamanPagerAdapter(
        fragment: Fragment,
        private val halamanList: List<Halaman>,
        private val jilidId: Int
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = halamanList.size

        override fun createFragment(position: Int): Fragment {
            val halaman = halamanList[position]
            return HalamanFragment.newInstance(
                jilidId = jilidId,
                halamanId = halaman.id,
                halamanIndex = position,
                totalHalaman = halamanList.size,
                isUnlocked = halaman.isUnlocked
            )
        }
    }
}
