function pollStatus(projectId){
  const badge=document.getElementById('statusBadge');
  const indicator=document.getElementById('processingIndicator');
  const stage=document.getElementById('processingStage');
  if(!badge) return;

  const labels={
    CREATED:'Создано',
    UPLOADED:'Файл загружен',
    CONVERTING:'Конвертация',
    TRANSCRIBING:'Распознавание',
    POST_PROCESSING:'Постобработка',
    ANALYZING:'Анализ',
    READY:'Готово',
    ERROR:'Ошибка'
  };

  const processingStatuses=['CONVERTING','TRANSCRIBING','POST_PROCESSING','ANALYZING'];

  const update=(d)=>{
    const status=d.status;
    badge.innerText=labels[status]||status;
    badge.className='badge rounded-pill status-badge';
    if(status==='READY') badge.classList.add('bg-success');
    else if(status==='ERROR') badge.classList.add('bg-danger');
    else if(status==='UPLOADED') badge.classList.add('text-bg-light');
    else badge.classList.add('bg-navy');

    if(indicator){
      if(processingStatuses.includes(status)){
        indicator.classList.remove('d-none');
        if(stage) stage.innerText=labels[status]||status;
      }else{
        indicator.classList.add('d-none');
      }
    }
  };

  setInterval(()=>{
    fetch(`/projects/${projectId}/status`).then(r=>r.json()).then(update).catch(()=>{});
  },3000);
}
