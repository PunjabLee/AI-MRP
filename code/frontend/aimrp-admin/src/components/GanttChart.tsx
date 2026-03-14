/**
 * 甘特图组件
 */
import { useState } from 'react';

interface GanttTask {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  progress: number;
  dependencies?: string[];
}

interface GanttProps {
  tasks: GanttTask[];
  onTaskClick?: (task: GanttTask) => void;
}

export function GanttChart({ tasks, onTaskClick }: GanttProps) {
  const [selectedTask, setSelectedTask] = useState<string | null>(null);
  
  // 计算时间范围
  const dates: string[] = [];
  const startDates = tasks.map(t => new Date(t.startDate).getTime());
  const endDates = tasks.map(t => new Date(t.endDate).getTime());
  const minDate = new Date(Math.min(...startDates));
  const maxDate = new Date(Math.max(...endDates));
  
  // 生成日期序列
  for (let d = new Date(minDate); d <= maxDate; d.setDate(d.getDate() + 1)) {
    dates.push(d.toISOString().split('T')[0]);
  }
  
  // 计算任务位置
  const getTaskStyle = (task: GanttTask) => {
    const startIdx = dates.indexOf(task.startDate);
    const endIdx = dates.indexOf(task.endDate);
    const left = (startIdx / dates.length) * 100;
    const width = ((endIdx - startIdx + 1) / dates.length) * 100;
    
    return {
      left: `${left}%`,
      width: `${width}%`,
    };
  };
  
  // 格式化日期
  const formatDate = (dateStr: string) => {
    const d = new Date(dateStr);
    return `${d.getMonth() + 1}/${d.getDate()}`;
  };
  
  const handleTaskClick = (task: GanttTask) => {
    setSelectedTask(task.id);
    onTaskClick?.(task);
  };
  
  return (
    <div className="bg-white rounded shadow overflow-hidden">
      {/* 表头 */}
      <div className="flex border-b">
        <div className="w-48 p-3 font-medium bg-gray-50 border-r">任务</div>
        <div className="flex-1 overflow-x-auto">
          <div className="flex min-w-max">
            {dates.map((date, idx) => (
              <div 
                key={date} 
                className={`w-12 p-2 text-xs text-center border-r ${
                  new Date(date).getDay() === 0 || new Date(date).getDay() === 6 
                    ? 'bg-gray-100' : ''
                }`}
              >
                {formatDate(date)}
              </div>
            ))}
          </div>
        </div>
      </div>
      
      {/* 任务列表 */}
      <div className="max-h-96 overflow-y-auto">
        {tasks.map(task => (
          <div key={task.id} className="flex border-b hover:bg-gray-50">
            <div className="w-48 p-3 text-sm border-r truncate cursor-pointer hover:text-blue-600"
                 onClick={() => handleTaskClick(task)}>
              {task.name}
            </div>
            <div className="flex-1 relative h-12">
              {/* 网格线 */}
              <div className="absolute inset-0 flex">
                {dates.map(date => (
                  <div 
                    key={date} 
                    className={`flex-1 border-r ${
                      new Date(date).getDay() === 0 || new Date(date).getDay() === 6 
                        ? 'bg-gray-50' : ''
                    }`}
                  />
                ))}
              </div>
              
              {/* 任务条 */}
              <div 
                className={`absolute top-2 h-8 rounded flex items-center px-2 cursor-pointer transition-all ${
                  selectedTask === task.id 
                    ? 'bg-blue-600 ring-2 ring-blue-300' 
                    : 'bg-blue-500 hover:bg-blue-600'
                }`}
                style={getTaskStyle(task)}
                onClick={() => handleTaskClick(task)}
              >
                <span className="text-white text-xs truncate">{task.progress}%</span>
              </div>
            </div>
          </div>
        ))}
      </div>
      
      {/* 图例 */}
      <div className="p-3 bg-gray-50 border-t flex gap-4 text-xs">
        <div className="flex items-center gap-1">
          <div className="w-4 h-4 bg-blue-500 rounded"></div>
          <span>计划中</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-4 h-4 bg-green-500 rounded"></div>
          <span>进行中</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-4 h-4 bg-gray-400 rounded"></div>
          <span>已完成</span>
        </div>
      </div>
    </div>
  );
}

/**
 * 排程结果展示页面
 */
export function SchedulerPage() {
  const [tasks, setTasks] = useState<GanttTask[]>([
    { id: '1', name: 'MO001 - 产品A', startDate: '2024-01-01', endDate: '2024-01-05', progress: 100 },
    { id: '2', name: 'MO002 - 产品B', startDate: '2024-01-03', endDate: '2024-01-08', progress: 60 },
    { id: '3', name: 'MO003 - 产品C', startDate: '2024-01-06', endDate: '2024-01-12', progress: 30 },
    { id: '4', name: 'MO004 - 产品D', startDate: '2024-01-10', endDate: '2024-01-15', progress: 0 },
  ]);
  
  const handleTaskClick = (task: GanttTask) => {
    console.log('点击任务:', task);
  };
  
  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="text-2xl font-bold">生产排程</h2>
        <button className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
          重新排程
        </button>
      </div>
      
      {/* 统计卡片 */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-white p-4 rounded shadow">
          <p className="text-sm text-gray-500">总工单</p>
          <p className="text-2xl font-bold">{tasks.length}</p>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <p className="text-sm text-gray-500">进行中</p>
          <p className="text-2xl font-bold text-blue-600">
            {tasks.filter(t => t.progress > 0 && t.progress < 100).length}
          </p>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <p className="text-sm text-gray-500">已完成</p>
          <p className="text-2xl font-bold text-green-600">
            {tasks.filter(t => t.progress === 100).length}
          </p>
        </div>
        <div className="bg-white p-4 rounded shadow">
          <p className="text-sm text-gray-500">未开始</p>
          <p className="text-2xl font-bold text-gray-400">
            {tasks.filter(t => t.progress === 0).length}
          </p>
        </div>
      </div>
      
      {/* 甘特图 */}
      <GanttChart tasks={tasks} onTaskClick={handleTaskClick} />
    </div>
  );
}
