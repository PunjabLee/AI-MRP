import { useState, useEffect } from 'react';

interface Organization {
  id: number;
  orgCode: string;
  orgName: string;
  orgType: string;
  level: number;
  parentId?: number;
  manager?: string;
  status: string;
  children?: Organization[];
}

export function OrgPage() {
  const [orgTree, setOrgTree] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState<Partial<Organization>>({});

  useEffect(() => {
    fetchOrgTree();
  }, []);

  const fetchOrgTree = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/org/tree');
      const data = await res.json();
      if (data.code === 200) {
        setOrgTree(data.data || []);
      }
    } catch (error) {
      console.error('获取组织失败', error);
    }
    setLoading(false);
  };

  const handleCreate = async () => {
    try {
      await fetch('/api/org', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      setShowModal(false);
      setFormData({});
      fetchOrgTree();
    } catch (error) {
      console.error('创建失败', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除吗?')) return;
    try {
      await fetch(`/api/org/${id}`, { method: 'DELETE' });
      fetchOrgTree();
    } catch (error) {
      console.error('删除失败', error);
    }
  };

  const renderTree = (nodes: Organization[], level = 0) => {
    return nodes.map(node => (
      <div key={node.id} style={{ marginLeft: level * 20 }}>
        <div className="flex items-center p-2 hover:bg-gray-50 border-b">
          <span className="font-medium">{node.orgName}</span>
          <span className="ml-2 text-gray-500 text-sm">({node.orgCode})</span>
          <span className="ml-2 px-2 py-0.5 text-xs rounded bg-blue-100">{node.orgType}</span>
          <span className={`ml-2 px-2 py-0.5 text-xs rounded ${node.status === 'ENABLED' ? 'bg-green-100' : 'bg-red-100'}`}>
            {node.status}
          </span>
          <button 
            onClick={() => handleDelete(node.id)}
            className="ml-auto text-red-600 hover:underline text-sm"
          >
            删除
          </button>
        </div>
        {node.children && node.children.length > 0 && renderTree(node.children, level + 1)}
      </div>
    ));
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">组织架构</h1>
        <button
          onClick={() => setShowModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          新增组织
        </button>
      </div>

      <div className="bg-white rounded-lg shadow">
        {loading ? (
          <div className="p-4 text-center text-gray-500">加载中...</div>
        ) : (
          renderTree(orgTree)
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">新增组织</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">组织编码</label>
                <input
                  type="text"
                  value={formData.orgCode || ''}
                  onChange={e => setFormData({...formData, orgCode: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">组织名称</label>
                <input
                  type="text"
                  value={formData.orgName || ''}
                  onChange={e => setFormData({...formData, orgName: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">组织类型</label>
                <select
                  value={formData.orgType || ''}
                  onChange={e => setFormData({...formData, orgType: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                >
                  <option value="">请选择</option>
                  <option value="COMPANY">公司</option>
                  <option value="DEPARTMENT">部门</option>
                  <option value="WORKSHOP">车间</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">负责人</label>
                <input
                  type="text"
                  value={formData.manager || ''}
                  onChange={e => setFormData({...formData, manager: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-6">
              <button onClick={() => setShowModal(false)} className="px-4 py-2 border rounded-lg">
                取消
              </button>
              <button onClick={handleCreate} className="px-4 py-2 bg-blue-600 text-white rounded-lg">
                创建
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
