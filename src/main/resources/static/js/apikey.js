/* global Vue, axios, ELEMENT */

var API_BASE = window.location.origin;
axios.defaults.baseURL = API_BASE;

// 会话过期(401)自动回登录页
axios.interceptors.response.use(function (res) { return res; }, function (err) {
    if (err.response && err.response.status === 401) {
        window.location.href = '/login.html';
    }
    return Promise.reject(err);
});

new Vue({
    el: '#app',
    data: {
        keys: [],
        createVisible: false,
        createForm: { name: '' },
        creating: false,
        revealVisible: false,
        newKey: ''
    },
    methods: {
        // 加载密钥列表
        loadKeys: function () {
            var self = this;
            axios.get('/api/apikey/list').then(function (res) {
                if (res.data.code === 200) {
                    self.keys = res.data.data || [];
                } else {
                    ELEMENT.Message.error(res.data.message || '加载密钥列表失败');
                }
            }).catch(function () {
                ELEMENT.Message.error('加载密钥列表失败');
            });
        },
        // 打开创建对话框
        showCreateDialog: function () {
            this.createForm = { name: '' };
            this.createVisible = true;
        },
        // 创建密钥
        createKey: function () {
            var self = this;
            self.creating = true;
            axios.post('/api/apikey/create', { name: self.createForm.name.trim() || null }).then(function (res) {
                if (res.data.code === 200) {
                    self.newKey = res.data.data.key;
                    self.createVisible = false;
                    self.revealVisible = true;
                    self.loadKeys();
                } else {
                    ELEMENT.Message.error(res.data.message || '创建失败');
                }
            }).catch(function () {
                ELEMENT.Message.error('创建失败');
            }).finally(function () {
                self.creating = false;
            });
        },
        // 复制新密钥
        copyNewKey: function () {
            copyText(this.newKey);
        },
        // 撤销密钥
        revokeKey: function (row) {
            var self = this;
            ELEMENT.MessageBox.confirm(
                '确定撤销密钥 ' + (row.name || row.keyPreview) + ' 吗？撤销后该密钥将无法再调用数据接口。',
                '确认撤销',
                { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
            ).then(function () {
                axios.delete('/api/apikey/' + row.id).then(function (res) {
                    if (res.data.code === 200) {
                        ELEMENT.Message.success('已撤销');
                        self.loadKeys();
                    } else {
                        ELEMENT.Message.error(res.data.message || '撤销失败');
                    }
                }).catch(function () {
                    ELEMENT.Message.error('撤销失败');
                });
            }).catch(function () {});
        },
        // 退出登录
        logout: function () {
            axios.post('/logout').finally(function () {
                window.location.href = '/login.html?logout=1';
            });
        },
        // 返回数据浏览器
        goToBrowser: function () {
            window.location.href = 'index.html';
        },
        // 时间格式化
        formatDate: function (val) {
            if (!val) return '';
            var d = new Date(val);
            if (isNaN(d.getTime())) return String(val);
            var pad = function (n) { return n < 10 ? '0' + n : n; };
            return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
                + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
        }
    },
    mounted: function () {
        this.loadKeys();
    }
});

function copyText(text) {
    if (!text) {
        ELEMENT.Message.warning('没有可复制的密钥');
        return;
    }
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(function () {
            ELEMENT.Message.success('已复制到剪贴板');
        }).catch(function () {
            ELEMENT.Message.warning('复制失败，请手动选择复制');
        });
    } else {
        ELEMENT.Message.warning('当前浏览器不支持自动复制，请手动选择复制');
    }
}
