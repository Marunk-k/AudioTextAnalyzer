const STATUS_LABELS={CREATED:'Создан',UPLOADED:'Файл загружен',CONVERTING:'Конвертация',TRANSCRIBING:'Распознавание',POST_PROCESSING:'Постобработка',ANALYZING:'Анализ',READY:'Готово',ERROR:'Ошибка'};
const PROCESSING_STATUSES=new Set(['CONVERTING','TRANSCRIBING','POST_PROCESSING','ANALYZING']);

function pollStatus(projectId){
  const badge=document.getElementById('statusBadge');
  const processingAlert=document.getElementById('processingAlert');
  const processingText=document.getElementById('processingStatusText');
  if(!badge) return;

  const processForm=document.getElementById('processForm');
  if(processForm){
    processForm.addEventListener('submit',()=>{
      const btn=document.getElementById('processBtn');
      const spinner=document.getElementById('processBtnSpinner');
      const text=btn?.querySelector('.btn-text');
      if(btn) btn.disabled=true;
      if(spinner) spinner.classList.remove('d-none');
      if(text) text.textContent='Обработка...';
      if(processingAlert) processingAlert.classList.remove('d-none');
      if(processingText) processingText.textContent='Конвертация';
    });
  }

  setInterval(()=>{fetch(`/projects/${projectId}/status`).then(r=>r.json()).then(d=>{
    const status=d.status;
    badge.innerText=STATUS_LABELS[status]||status;
    if(processingAlert){
      if(PROCESSING_STATUSES.has(status)){processingAlert.classList.remove('d-none');}
      else{processingAlert.classList.add('d-none');}
    }
    if(processingText){processingText.textContent=STATUS_LABELS[status]||status;}
  });},3000);
}
