package com.example.sqlite.data

import kotlinx.coroutines.flow.Flow

// El repositorio es el intermediario entre el ViewModel y el DAO.
class TrasladoRepository(private val dao: TrasladoDao) {

    val traslados: Flow<List<Traslado>> = dao.getAll()
    val destinos: Flow<List<Destino>> = dao.getDestinos()

    suspend fun agregar(traslado: Traslado) {
        dao.insert(traslado)
    }

    suspend fun actualizar(traslado: Traslado) = dao.update(traslado)

    suspend fun eliminar(traslado: Traslado) = dao.delete(traslado)

    // Pre-carga el catálogo de destinos solo si la tabla está vacía (así nunca se duplican)
    suspend fun precargarDestinos() {
        if (dao.contarDestinos() == 0) {
            dao.insertarDestinos(CatalogoDestinos.iniciales)
        }
    }
}
