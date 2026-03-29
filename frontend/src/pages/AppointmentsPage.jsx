useEffect(() => {
  fetch("/api/appointments").then(r => r.json()).then(setAppointments);
}, []);