package com.example.iqromandarin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iqromandarin.adapter.JilidAdapter
import com.example.iqromandarin.database.AppDatabase
import com.example.iqromandarin.databinding.FragmentDashboardBinding
import com.example.iqromandarin.model.Jilid
import com.example.iqromandarin.viewmodel.MainViewModel
import com.example.iqromandarin.viewmodel.MainViewModelFactory

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getDatabase(requireContext()))
    }

    private lateinit var jilidAdapter: JilidAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).supportActionBar?.apply {
            title = "汉语拼音 · Iqro Mandarin"
            setDisplayHomeAsUpEnabled(false)
        }

        setupMenu()
        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_dark_mode -> {
                        (requireActivity() as MainActivity).toggleDarkMode()
                        true
                    }
                    R.id.action_review -> {
                        navigateToReview()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        jilidAdapter = JilidAdapter { jilid ->
            navigateToJilid(jilid)
        }
        binding.rvJilid.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = jilidAdapter
        }
    }

    private fun observeData() {
        viewModel.allJilid.observe(viewLifecycleOwner) { jilidList ->
            jilidAdapter.submitList(jilidList)
        }

        viewModel.totalItemDikuasai.observe(viewLifecycleOwner) { count ->
            binding.tvItemDikuasai.text = count.toString()
        }

        viewModel.currentJilidNumber.observe(viewLifecycleOwner) { num ->
            binding.tvJilidSekarang.text = "Jilid $num"
        }

        viewModel.totalHalamanSelesai.observe(viewLifecycleOwner) { count ->
            binding.tvHalamanSelesai.text = count.toString()
        }

        viewModel.dailyReviewCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                binding.btnDailyReview.visibility = View.VISIBLE
                binding.btnDailyReview.text = "📋 Review Hari Ini ($count)"
            } else {
                binding.btnDailyReview.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnDailyReview.setOnClickListener {
            navigateToReview()
        }

        binding.btnStartLearning.setOnClickListener {
            viewModel.currentActiveJilid.value?.let { jilid ->
                navigateToJilid(jilid)
            }
        }
    }

    private fun navigateToJilid(jilid: Jilid) {
        val fragment = JilidFragment.newInstance(jilid.id)
        (requireActivity() as MainActivity).loadFragment(fragment, "Jilid ${jilid.nomorJilid}: ${jilid.nama}")
    }

    private fun navigateToReview() {
        val fragment = ReviewFragment()
        (requireActivity() as MainActivity).loadFragment(fragment, "Review SRS")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
