// @flow
import { commercialFeatures } from 'common/modules/commercial/commercial-features';
import { initThirdPartyTags, _ } from './third-party-tags';

const { insertScripts, loadOther } = _;

beforeEach(() => {
    const firstScript = document.createElement('script');
    if (document.body && firstScript) {
        document.body.appendChild(firstScript);
    }
    expect.hasAssertions();
});

afterEach(() => {
    if (document.body) {
        document.body.innerHTML = '';
    }
});

jest.mock('commercial-control/modules/third-party-tags/outbrain', () => ({
    initOutbrain: jest.fn(),
}));

jest.mock('commercial-control/modules/third-party-tags/plista', () => ({
    plista: jest.fn(),
}));

jest.mock('common/modules/commercial/commercial-features', () => ({
    commercialFeatures: {
        thirdPartyTags: true,
    },
}));

jest.mock('commercial-control/modules/third-party-tags/imr-worldwide', () => ({
    imrWorldwide: {
        shouldRun: true,
        url: '//fakeThirdPartyTag.js',
        onLoad: jest.fn(),
    },
}));

describe('third party tags', () => {
    it('should exist', () => {
        expect(initThirdPartyTags).toBeDefined();
        expect(loadOther).toBeDefined();
        expect(insertScripts).toBeDefined();
    });

    it('should not run if disabled in commercial features', done => {
        commercialFeatures.thirdPartyTags = false;
        initThirdPartyTags()
            .then((enabled: boolean) => {
                expect(enabled).toBe(false);
                done();
            })
            .catch(() => {
                done.fail('third-party tags failed');
            });
    });

    describe('insertScripts', () => {
        const fakeThirdPartyTag: ThirdPartyTag = {
            shouldRun: true,
            url: '//fakeThirdPartyTag.js',
            onLoad: jest.fn(),
        };
        it('should add a script to the document', () => {
            insertScripts([fakeThirdPartyTag]);
            expect(document.scripts.length).toBe(2);
        });
    });
    describe('loadOther', () => {
        it('should call insert scripts', () => {
            loadOther();
            expect(document.scripts.length).toBe(2);
        });
    });
});
