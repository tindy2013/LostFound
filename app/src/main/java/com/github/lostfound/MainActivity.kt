package com.github.lostfound

import android.os.Bundle
import com.github.lostfound.databinding.ActivityMainBinding
import me.yokeyword.fragmentation.SupportFragment

class MainActivity: BaseActivity() {
    private val fragments: Array<SupportFragment?> = arrayOfNulls(FRAGMENT_NUM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (findFragment(GroundFragment::class.java) == null) {
            fragments[FRAGMENT_GROUND] = GroundFragment()
            fragments[FRAGMENT_SEARCH] = SearchFragment()
            fragments[FRAGMENT_MYSELF] = MyselfFragment()
            loadMultipleRootFragment(binding.frame.id, 0, *fragments)
        } else {
            fragments[FRAGMENT_GROUND] = findFragment(GroundFragment::class.java)
            fragments[FRAGMENT_SEARCH] = findFragment(SearchFragment::class.java)
            fragments[FRAGMENT_MYSELF] = findFragment(MyselfFragment::class.java)
        }

        binding.navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_ground -> {
                    showHideFragment(fragments[FRAGMENT_GROUND])
                    true
                }
                R.id.nav_search -> {
                    showHideFragment(fragments[FRAGMENT_SEARCH])
                    true
                }
                R.id.nav_my -> {
                    showHideFragment(fragments[FRAGMENT_MYSELF])
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private const val FRAGMENT_GROUND = 0
        private const val FRAGMENT_SEARCH = 1
        private const val FRAGMENT_MYSELF = 2
        private const val FRAGMENT_NUM = 3
    }
}