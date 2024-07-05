module.exports = {
    extends: ['@commitlint/config-conventional'],
    parserPreset: {
        parserOpts: {
            headerPattern: /^(\w+)(?:\((\w+)\))?: (.*) \[macata #(\d+)]( \(#\d+\))?$/,
            headerCorrespondence: ['type', 'scope', 'subject', 'issue'],
        },
    },
    rules: {
        'type-enum': [2, 'always', [
            'build',
            'chore',
            'ci',
            'docs',
            'feat',
            'fix',
            'perf',
            'refactor',
            'revert',
            'style',
            'test',
            'wip'
        ] ],
        'header-max-length': [2, 'always', 100],
        'references-empty': [2, 'never']
    },
};