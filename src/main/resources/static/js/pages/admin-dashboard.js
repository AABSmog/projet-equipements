/* Dashboard administration */
window.PAGE_INIT = async function () {
  try {
    const stats = await Api.get('/api/admin/stats');
    document.getElementById('stat-equipements').textContent = stats.totalEquipements;
    document.getElementById('stat-affectations').textContent = stats.affectationsEnCours;
    document.getElementById('stat-signalements').textContent = stats.signalementsOuverts;
    document.getElementById('stat-personnel').textContent = stats.totalPersonnel;
  } catch (e) {
    console.error(e);
  }
};