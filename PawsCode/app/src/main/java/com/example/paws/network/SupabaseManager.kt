package com.example.paws.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {
    
    private const val SUPABASE_URL = "https://yjddbkmvjchuocwbpczv.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_a1Y07txTriiNK-ju_Cmvkw_XQw49hA9"
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Storage)
            install(Postgrest)
        }
    }
}